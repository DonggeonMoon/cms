package com.dgmoonlabs.cms.domain.course.service;

import com.dgmoonlabs.cms.domain.business.category.constant.CategoryDomain;
import com.dgmoonlabs.cms.domain.business.category.service.CategoryService;
import com.dgmoonlabs.cms.domain.business.order.constant.OrderStatus;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderService;
import com.dgmoonlabs.cms.domain.common.file.dto.FileResponse;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.common.file.service.FileService.DownloadableFile;
import com.dgmoonlabs.cms.domain.common.grade.service.GradeService;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.domain.course.constant.AccessPeriodMode;
import com.dgmoonlabs.cms.domain.course.constant.CourseApplicationStatus;
import com.dgmoonlabs.cms.domain.course.constant.EnrollmentMode;
import com.dgmoonlabs.cms.domain.course.constant.ScorePolicy;
import com.dgmoonlabs.cms.domain.course.dto.CourseDtos.*;
import com.dgmoonlabs.cms.domain.course.entity.*;
import com.dgmoonlabs.cms.domain.course.repository.CourseApplicationRepository;
import com.dgmoonlabs.cms.domain.course.repository.CourseEnrollmentRepository;
import com.dgmoonlabs.cms.domain.course.repository.CourseLessonCommentRepository;
import com.dgmoonlabs.cms.domain.course.repository.CourseLessonRepository;
import com.dgmoonlabs.cms.domain.course.repository.CourseMaterialRepository;
import com.dgmoonlabs.cms.domain.course.repository.CourseRepository;
import com.dgmoonlabs.cms.domain.course.repository.QuizAttemptGrantRepository;
import com.dgmoonlabs.cms.domain.course.repository.QuizAttemptRepository;
import com.dgmoonlabs.cms.domain.course.repository.QuizRepository;
import com.dgmoonlabs.cms.domain.course.repository.QuizSessionRepository;
import com.dgmoonlabs.cms.domain.streaming.constant.EnrollmentStatus;
import com.dgmoonlabs.cms.domain.streaming.entity.Streaming;
import com.dgmoonlabs.cms.domain.streaming.entity.StreamingEnrollment;
import com.dgmoonlabs.cms.domain.streaming.entity.StreamingProgress;
import com.dgmoonlabs.cms.domain.streaming.repository.StreamingEnrollmentRepository;
import com.dgmoonlabs.cms.domain.streaming.repository.StreamingProgressRepository;
import com.dgmoonlabs.cms.domain.streaming.repository.StreamingRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import com.dgmoonlabs.cms.global.site.SiteContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CourseService {
    private static final int MAX_COMMENT_DEPTH = 1;

    private final CourseRepository courseRepository;
    private final CourseLessonRepository lessonRepository;
    private final CourseLessonCommentRepository lessonCommentRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseApplicationRepository applicationRepository;
    private final CourseMaterialRepository materialRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final QuizAttemptGrantRepository attemptGrantRepository;
    private final QuizRepository quizRepository;
    private final StreamingRepository streamingRepository;
    private final StreamingEnrollmentRepository streamingEnrollmentRepository;
    private final StreamingProgressRepository streamingProgressRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;
    private final GradeService gradeService;
    private final CourseSalesService courseSalesService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher events;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public Page<CourseResponse> getCourses(final String keyword, final Boolean isCourse, final Pageable pageable) {
        String term = keyword == null ? "" : keyword;
        Long siteId = SiteContextHolder.currentSiteId();
        Page<Course> courses = CurrentUser.isAdmin()
                ? courseRepository.searchAdmin(siteId, term, isCourse, pageable)
                : courseRepository.searchPublic(siteId, term, isCourse, pageable);
        Map<Long, Long> counts = lessonRepository.findAllByCourseIdInAndDeletedAtIsNullOrderBySequenceAsc(
                        courses.map(Course::getId).toList()).stream()
                .collect(Collectors.groupingBy(CourseLesson::getCourseId, Collectors.counting()));
        return courses.map(course -> summary(course, counts.getOrDefault(course.getId(), 0L).intValue()));
    }

    /** 강의 시청은 스트리밍에서 기록되므로, 과정을 볼 때 차시 완료를 다시 계산해 과정 완료를 맞춘다. */
    @Transactional
    public CourseResponse getCourse(final Long id) {
        boolean admin = CurrentUser.isAdmin();
        Course course = admin ? findCourse(id) : findPublic(id);
        if (!admin) {
            gradeService.require(course.getAccessGradeId());
        }
        List<CourseLesson> lessons = lessonsOf(id);
        Long memberId = CurrentUser.isMember() ? CurrentUser.get().map(LoginUser::id).orElse(null) : null;
        Progress progress = memberId == null ? Progress.EMPTY : progressOf(memberId, lessons);
        MyCourseProgress mine = memberId == null ? null : syncEnrollment(course, memberId, lessons, progress);
        return detail(course, lessons, progress, mine, admin, memberId);
    }

    /** 관리자가 수강자 한 명의 항목별 수강 기록(완료 여부·시청 진도율·마지막 위치·퀴즈 점수)을 보는 화면용. */
    @Transactional
    public CourseResponse getLearnerProgress(final Long courseId, final Long memberId) {
        Course course = findCourse(courseId);
        enrollmentRepository.findByCourseIdAndMemberId(courseId, memberId).orElseThrow(BusinessException::notFound);
        List<CourseLesson> lessons = lessonsOf(courseId);
        Progress progress = progressOf(memberId, lessons);
        MyCourseProgress mine = syncEnrollment(course, memberId, lessons, progress);
        return detail(course, lessons, progress, mine, true, memberId);
    }

    @Transactional
    public Long createCourse(final CourseSaveRequest request) {
        validate(request);
        boolean isCourse = request.isCourse();
        AccessPeriodMode periodMode = request.accessPeriodModeOrDefault();
        Course course = courseRepository.save(Course.builder()
                .siteId(SiteContextHolder.currentSiteId())
                .title(request.title().trim())
                .description(request.description())
                .thumbnailFileId(request.thumbnailFileId())
                .sequential(request.sequential())
                .accessGradeId(gradeService.validGradeId(request.accessGradeId()))
                .isUsing(request.isUsing())
                .isCourse(isCourse)
                .enrollmentMode(isCourse ? request.enrollmentModeOrDefault() : EnrollmentMode.FREE)
                .applicationStartAt(isCourse ? request.applicationStartAt() : null)
                .applicationEndAt(isCourse ? request.applicationEndAt() : null)
                .accessPeriodMode(isCourse ? periodMode : AccessPeriodMode.UNLIMITED)
                .accessStartAt(isCourse && periodMode == AccessPeriodMode.FIXED ? request.accessStartAt() : null)
                .accessEndAt(isCourse && periodMode == AccessPeriodMode.FIXED ? request.accessEndAt() : null)
                .accessDays(isCourse && periodMode == AccessPeriodMode.DAYS_FROM_ENROLLMENT ? request.accessDays() : null)
                .maxPlaybackRate(isCourse ? request.maxPlaybackRate() : null)
                .preventSkipping(isCourse && request.preventSkipping())
                .completionThresholdPercent(isCourse ? request.completionThresholdPercent() : null)
                .attendanceRequired(isCourse && periodMode != AccessPeriodMode.UNLIMITED && request.attendanceRequired())
                .trackProgress(request.trackProgressOrDefault())
                .completionRate(request.completionRateOrDefault())
                .build());
        syncLessons(course.getId(), request.lessons());
        syncMaterials(course.getId(), null, request.materialFileIdsOrEmpty());
        applySale(course, request);
        changeCategory(course, request);
        return course.getId();
    }

    @Transactional
    public void updateCourse(final Long id, final CourseSaveRequest request) {
        validate(request);
        Course course = findCourse(id);
        course.update(request.title().trim(), request.description(), request.thumbnailFileId(), request.sequential(),
                gradeService.validGradeId(request.accessGradeId()), request.isUsing(), request.isCourse(), request.enrollmentModeOrDefault(),
                request.applicationStartAt(), request.applicationEndAt(), request.accessPeriodModeOrDefault(),
                request.accessStartAt(), request.accessEndAt(), request.accessDays(), request.maxPlaybackRate(), request.preventSkipping(),
                request.completionThresholdPercent(), request.attendanceRequired(), request.trackProgressOrDefault(), request.completionRateOrDefault());
        syncLessons(id, request.lessons());
        syncMaterials(id, null, request.materialFileIdsOrEmpty());
        applySale(course, request);
        changeCategory(course, request);
    }

    private void changeCategory(final Course course, final CourseSaveRequest request) {
        course.changeCategory(request.categoryId() == null ? null
                : categoryService.findForDomain(request.categoryId(), CategoryDomain.COURSE).getId());
    }

    @Transactional
    public void deleteCourse(final Long id) {
        Course course = findCourse(id);
        course.delete();
        lessonsOf(id).forEach(CourseLesson::delete);
        courseSalesService.syncProduct(course);
    }

    private void applySale(final Course course, final CourseSaveRequest request) {
        course.changeSale(request.paid(), request.price());
        course.changeRefundPolicy(request.refundableProgressBelowPercent());
        courseSalesService.syncProduct(course);
    }

    /** 수강 권한(수강 기록이 있고 기간 안)이 있는 사용자만, 관리자는 언제나 학습자료를 받을 수 있다. */
    @Transactional
    public DownloadableFile downloadMaterial(final Long courseId, final Long materialId) {
        boolean admin = CurrentUser.isAdmin();
        Course course = admin ? findCourse(courseId) : findPublic(courseId);
        CourseMaterial material = materialRepository.findByIdAndCourseId(materialId, courseId).orElseThrow(BusinessException::notFound);
        if (!admin) {
            gradeService.require(course.getAccessGradeId());
            Long memberId = CurrentUser.get().filter(user -> user.userType() == UserType.MEMBER).map(LoginUser::id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
            CourseEnrollment enrollment = enrollmentRepository.findByCourseIdAndMemberId(courseId, memberId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_ENROLLED));
            if (enrollment.isRevoked()) {
                throw new BusinessException(ErrorCode.COURSE_ACCESS_REVOKED);
            }
            if (!course.accessOpen(enrollment.getCreatedAt(), enrollment.getAccessExtendedUntil(), LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.COURSE_ACCESS_PERIOD_ENDED);
            }
        }
        return fileService.downloadProtected(material.getFileId());
    }

    /**
     * "수강 신청" 버튼의 동작. 수강 신청 방식(자유 수강·신청 후 수강·승인제)에 따라
     * 바로 수강 기록을 만들거나, 신청 기록만 남기고 관리자 승인을 기다린다.
     */
    @Transactional
    public CourseResponse enroll(final Long courseId, final LoginUser member) {
        Course course = findPublic(courseId);
        gradeService.require(course.getAccessGradeId());
        if (course.isPaid()) {
            throw new BusinessException(ErrorCode.COURSE_PURCHASE_REQUIRED);
        }
        boolean alreadyEnrolled = enrollmentRepository.findByCourseIdAndMemberId(courseId, member.id()).isPresent();
        CourseApplication active = applicationRepository.findFirstByCourseIdAndMemberIdOrderByIdDesc(courseId, member.id())
                .filter(this::isActive).orElse(null);
        if (!alreadyEnrolled && active == null && !course.applicationOpen(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.COURSE_APPLICATION_PERIOD_CLOSED);
        }
        EnrollmentMode mode = course.effectiveEnrollmentMode();
        if (mode == EnrollmentMode.FREE) {
            ensureEnrolled(courseId, member.id());
        } else if (active == null) {
            CourseApplication application = applicationRepository.save(CourseApplication.apply(courseId, member.id()));
            events.publishEvent(new CourseApplicationAppliedEvent(application.getId(), courseId, member.id()));
            if (mode == EnrollmentMode.APPLY) {
                application.approve(LocalDateTime.now());
                ensureEnrolled(courseId, member.id());
                events.publishEvent(new CourseApplicationApprovedEvent(application.getId(), courseId, member.id()));
            }
        }
        return getCourse(courseId);
    }

    /** 관리자가 수강자 한 명의 수강 가능 기간을 늘려(또는 줄여) 준다. null이면 연장을 없앤다. */
    @Transactional
    public void extendAccess(final Long courseId, final Long memberId, final LocalDateTime accessUntil) {
        CourseEnrollment enrollment = enrollmentRepository.findByCourseIdAndMemberId(courseId, memberId).orElseThrow(BusinessException::notFound);
        enrollment.extendAccess(accessUntil);
    }

    /**
     * 결제로 수강 중인 회원을 환불 처리한다. 환불 정책(진도율 기준)을 넘겼으면 막는다.
     * 실제 환불·수강 권한 회수는 기존 주문 취소 로직(OrderService)과 그 이벤트(OrderRefundedEvent)를 그대로 탄다.
     */
    @Transactional
    public void refundEnrollment(final Long courseId, final Long memberId) {
        Course course = findCourse(courseId);
        CourseEnrollment enrollment = enrollmentRepository.findByCourseIdAndMemberId(courseId, memberId).orElseThrow(BusinessException::notFound);
        if (enrollment.getOrderId() == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_PAID);
        }
        if (course.getRefundableProgressBelowPercent() != null) {
            List<CourseLesson> lessons = lessonsOf(courseId);
            int progressPercent = lessons.isEmpty() ? 0 : progressOf(memberId, lessons).completedCount(lessons) * 100 / lessons.size();
            if (progressPercent >= course.getRefundableProgressBelowPercent()) {
                throw new BusinessException(ErrorCode.COURSE_REFUND_NOT_ALLOWED);
            }
        }
        orderService.changeStatus(enrollment.getOrderId(), OrderStatus.CANCELED);
    }

    @Transactional
    public void cancelApplication(final Long courseId, final LoginUser member) {
        CourseApplication application = applicationRepository.findFirstByCourseIdAndMemberIdOrderByIdDesc(courseId, member.id())
                .filter(CourseApplication::isPending)
                .orElseThrow(BusinessException::notFound);
        application.cancel(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplications(final Long courseId, final Pageable pageable) {
        findCourse(courseId);
        Page<CourseApplication> applications = applicationRepository.findAllByCourseIdOrderByIdDesc(courseId, pageable);
        Map<Long, String> usernames = memberRepository.findAllById(applications.map(CourseApplication::getMemberId).toList()).stream()
                .collect(Collectors.toMap(Member::getId, Member::getUsername));
        return applications.map(application -> ApplicationResponse.of(application,
                usernames.getOrDefault(application.getMemberId(), "(탈퇴 회원)")));
    }

    /**
     * 과정별 수강 현황·완료율·매출 통계. 결제 수강은 CourseEnrollment.orderId로 주문과 1:1로 이어지므로,
     * 그 주문들을 한 번에 모아 상태별로 집계해서 매출(결제 완료)·환불 건수(취소)를 구한다.
     */
    @Transactional(readOnly = true)
    public CourseStatisticsResponse getStatistics() {
        List<Course> courses = courseRepository.findAllBySiteIdAndDeletedAtIsNullOrderByIdDesc(SiteContextHolder.currentSiteId());
        List<Long> courseIds = courses.stream().map(Course::getId).toList();

        Map<Long, List<CourseEnrollment>> enrollmentsByCourse = enrollmentRepository.findAllByCourseIdIn(courseIds).stream()
                .collect(Collectors.groupingBy(CourseEnrollment::getCourseId));
        Map<Long, List<CourseApplication>> applicationsByCourse = applicationRepository.findAllByCourseIdIn(courseIds).stream()
                .collect(Collectors.groupingBy(CourseApplication::getCourseId));
        List<Long> orderIds = enrollmentsByCourse.values().stream().flatMap(List::stream)
                .map(CourseEnrollment::getOrderId).filter(Objects::nonNull).toList();
        Map<Long, Order> ordersById = byKey(orderRepository.findAllById(orderIds), Order::getId);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalEnrolled = 0;
        int totalCompleted = 0;
        List<CourseStatisticsRow> rows = new ArrayList<>();
        for (Course course : courses) {
            List<CourseEnrollment> enrollments = enrollmentsByCourse.getOrDefault(course.getId(), List.of());
            List<CourseApplication> applications = applicationsByCourse.getOrDefault(course.getId(), List.of());
            int completed = (int) enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED).count();
            int revoked = (int) enrollments.stream().filter(CourseEnrollment::isRevoked).count();
            double completionRate = enrollments.isEmpty() ? 0 : completed * 100.0 / enrollments.size();

            List<Order> orders = enrollments.stream().map(CourseEnrollment::getOrderId).filter(Objects::nonNull)
                    .map(ordersById::get).filter(Objects::nonNull).toList();
            BigDecimal revenue = orders.stream().filter(o -> o.getStatus() == OrderStatus.PAID)
                    .map(Order::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

            rows.add(new CourseStatisticsRow(course.getId(), course.getTitle(), course.isCourse(), course.isPaid(),
                    enrollments.size(), enrollments.size() - completed, completed, revoked, completionRate,
                    applications.stream().filter(a -> a.getStatus() == CourseApplicationStatus.APPLIED).count(),
                    applications.stream().filter(a -> a.getStatus() == CourseApplicationStatus.APPROVED).count(),
                    applications.stream().filter(a -> a.getStatus() == CourseApplicationStatus.REJECTED).count(),
                    applications.stream().filter(a -> a.getStatus() == CourseApplicationStatus.CANCELED).count(),
                    revenue, orders.stream().filter(o -> o.getStatus() == OrderStatus.PAID).count(),
                    orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELED).count(),
                    orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING_PAYMENT).count()));
            totalRevenue = totalRevenue.add(revenue);
            totalEnrolled += enrollments.size();
            totalCompleted += completed;
        }
        return new CourseStatisticsResponse(rows, totalRevenue, totalEnrolled, totalCompleted);
    }

    @Transactional
    public void approveApplication(final Long courseId, final Long applicationId) {
        CourseApplication application = pendingApplication(courseId, applicationId);
        application.approve(LocalDateTime.now());
        ensureEnrolled(courseId, application.getMemberId());
        events.publishEvent(new CourseApplicationApprovedEvent(applicationId, courseId, application.getMemberId()));
    }

    @Transactional
    public void rejectApplication(final Long courseId, final Long applicationId) {
        pendingApplication(courseId, applicationId).reject(LocalDateTime.now());
    }

    private CourseApplication pendingApplication(final Long courseId, final Long applicationId) {
        CourseApplication application = applicationRepository.findByIdAndCourseId(applicationId, courseId).orElseThrow(BusinessException::notFound);
        if (!application.isPending()) {
            throw new BusinessException(ErrorCode.COURSE_APPLICATION_ALREADY_DECIDED);
        }
        return application;
    }

    private boolean isActive(final CourseApplication application) {
        return application.getStatus() != CourseApplicationStatus.REJECTED && application.getStatus() != CourseApplicationStatus.CANCELED;
    }

    private void ensureEnrolled(final Long courseId, final Long memberId) {
        enrollmentRepository.findByCourseIdAndMemberId(courseId, memberId)
                .orElseGet(() -> enrollmentRepository.save(CourseEnrollment.builder().courseId(courseId).memberId(memberId).build()));
    }

    private record QuizContext(Course course, List<CourseLesson> lessons, CourseLesson lesson, Quiz quiz) {
    }

    private QuizContext quizContext(final Long courseId, final Long lessonId, final LoginUser member) {
        Course course = findPublic(courseId);
        gradeService.require(course.getAccessGradeId());
        CourseEnrollment enrollment = enrollmentRepository.findByCourseIdAndMemberId(courseId, member.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_ENROLLED));
        if (enrollment.isRevoked()) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_REVOKED);
        }
        if (!course.accessOpen(enrollment.getCreatedAt(), enrollment.getAccessExtendedUntil(), LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_PERIOD_ENDED);
        }
        List<CourseLesson> lessons = lessonsOf(courseId);
        CourseLesson lesson = lessons.stream().filter(candidate -> candidate.getId().equals(lessonId)).findFirst()
                .filter(CourseLesson::hasQuiz)
                .orElseThrow(BusinessException::notFound);
        Progress before = progressOf(member.id(), lessons);
        if (isLocked(course, lessons, lesson, before)) {
            throw new BusinessException(ErrorCode.LESSON_LOCKED);
        }
        Quiz quiz = quizRepository.findById(lesson.getQuizId()).orElseThrow(BusinessException::notFound);
        return new QuizContext(course, lessons, lesson, quiz);
    }

    /** 문제·보기를 (설정에 따라) 섞어서 보여주고, 그 순서를 기억해 둔다. 제출 시 이 순서로 원래 문항·보기를 되찾아 채점한다. */
    @Transactional
    public QuizStartResponse startQuiz(final Long courseId, final Long lessonId, final LoginUser member) {
        QuizContext ctx = quizContext(courseId, lessonId, member);
        Quiz quiz = ctx.quiz();
        int used = (int) attemptRepository.countByLessonIdAndMemberId(lessonId, member.id());
        Integer allowed = effectiveAttemptLimit(quiz, extraAttemptsOf(lessonId, member.id()));
        if (allowed != null && used >= allowed) {
            throw new BusinessException(ErrorCode.QUIZ_RETAKE_LIMIT_REACHED);
        }
        List<QuizQuestion> questions = quiz.getQuestions();
        List<Integer> questionOrder = shuffledIndices(questions.size(), quiz.isShuffleQuestions());
        List<List<Integer>> optionOrders = new ArrayList<>();
        List<StartQuestion> startQuestions = new ArrayList<>();
        for (int originalIndex : questionOrder) {
            QuizQuestion question = questions.get(originalIndex);
            List<Integer> optionOrder = shuffledIndices(question.options().size(), quiz.isShuffleOptions());
            optionOrders.add(optionOrder);
            startQuestions.add(new StartQuestion(question.question(), optionOrder.stream().map(question.options()::get).toList()));
        }
        quizSessionRepository.deleteByLessonIdAndMemberId(lessonId, member.id());
        LocalDateTime now = LocalDateTime.now();
        quizSessionRepository.save(QuizSession.builder().lessonId(lessonId).memberId(member.id()).startedAt(now)
                .questionOrder(questionOrder).optionOrders(optionOrders).build());
        return new QuizStartResponse(startQuestions, quiz.getTimeLimitMinutes(), used, allowed, now);
    }

    @Transactional
    public QuizResult submitQuiz(final Long courseId, final Long lessonId, final LoginUser member, final QuizSubmitRequest request) {
        QuizContext ctx = quizContext(courseId, lessonId, member);
        Course course = ctx.course();
        List<CourseLesson> lessons = ctx.lessons();
        Quiz quiz = ctx.quiz();
        QuizSession session = quizSessionRepository.findByLessonIdAndMemberId(lessonId, member.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_STARTED));
        List<QuizQuestion> questions = quiz.getQuestions();
        List<Integer> questionOrder = session.getQuestionOrder();
        if (request.answers().size() != questionOrder.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        List<Boolean> correct = new ArrayList<>();
        List<Integer> correctOptionByPosition = new ArrayList<>();
        List<Integer> originalAnswers = new ArrayList<>(Collections.nCopies(questions.size(), -1));
        for (int position = 0; position < questionOrder.size(); position++) {
            QuizQuestion question = questions.get(questionOrder.get(position));
            List<Integer> optionOrder = session.getOptionOrders().get(position);
            int selected = request.answers().get(position);
            if (selected < 0 || selected >= optionOrder.size()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
            int selectedOriginalOption = optionOrder.get(selected);
            originalAnswers.set(questionOrder.get(position), selectedOriginalOption);
            correct.add(selectedOriginalOption == question.answerIndex());
            correctOptionByPosition.add(optionOrder.indexOf(question.answerIndex()));
        }
        int score = (int) Math.round(correct.stream().filter(Boolean::booleanValue).count() * 100.0 / correct.size());
        boolean passed = score >= quiz.getPassScore();
        int durationSeconds = Math.max(0, (int) Duration.between(session.getStartedAt(), LocalDateTime.now()).getSeconds());
        attemptRepository.save(QuizAttempt.builder().lessonId(lessonId).memberId(member.id()).answers(originalAnswers)
                .score(score).passed(passed).durationSeconds(durationSeconds).build());
        quizSessionRepository.deleteByLessonIdAndMemberId(lessonId, member.id());
        MyCourseProgress mine = syncEnrollment(course, member.id(), lessons, progressOf(member.id(), lessons));

        boolean revealed = switch (quiz.getRevealPolicy()) {
            case IMMEDIATELY -> true;
            case AFTER_PASS -> passed;
            case NEVER -> false;
        };
        List<Integer> answerIndexes = revealed ? correctOptionByPosition : null;
        List<String> explanations = revealed
                ? questionOrder.stream().map(index -> questions.get(index).explanation()).toList()
                : null;

        long used = attemptRepository.countByLessonIdAndMemberId(lessonId, member.id());
        Integer allowed = effectiveAttemptLimit(quiz, extraAttemptsOf(lessonId, member.id()));
        Integer remaining = allowed == null ? null : Math.max(0, allowed - (int) used);
        return new QuizResult(score, quiz.getPassScore(), passed, correct, durationSeconds, used, remaining,
                revealed, answerIndexes, explanations, mine);
    }

    /** 재응시 횟수를 모두 쓴 수강자에게 관리자가 더 응시할 수 있게 횟수를 더해 준다. */
    @Transactional
    public void grantQuizAttempts(final Long courseId, final Long lessonId, final Long memberId, final int count) {
        findCourse(courseId);
        CourseLesson lesson = lessonRepository.findById(lessonId)
                .filter(candidate -> candidate.getCourseId().equals(courseId) && candidate.hasQuiz())
                .orElseThrow(BusinessException::notFound);
        QuizAttemptGrant grant = attemptGrantRepository.findByLessonIdAndMemberId(lesson.getId(), memberId)
                .orElseGet(() -> attemptGrantRepository.save(QuizAttemptGrant.builder().lessonId(lesson.getId()).memberId(memberId).build()));
        grant.grant(count);
    }

    /** 모든 과정·항목을 통틀어 아직 답글(답변)이 달리지 않은 질문 목록. 관리자가 빠짐없이 답변하기 위한 화면에 쓴다. */
    @Transactional(readOnly = true)
    public Page<UnansweredQuestionResponse> getUnansweredQuestions(final Pageable pageable) {
        Page<CourseLessonComment> questions = lessonCommentRepository.findUnanswered(pageable);
        Map<Long, CourseLesson> lessons = byKey(lessonRepository.findAllById(questions.map(CourseLessonComment::getLessonId).toList()),
                CourseLesson::getId);
        Map<Long, Course> courses = byKey(courseRepository.findAllById(
                lessons.values().stream().map(CourseLesson::getCourseId).distinct().toList()), Course::getId);
        return questions.map(question -> {
            CourseLesson lesson = lessons.get(question.getLessonId());
            Course course = lesson == null ? null : courses.get(lesson.getCourseId());
            return new UnansweredQuestionResponse(question.getId(), course == null ? null : course.getId(),
                    course == null ? "(삭제됨)" : course.getTitle(), course != null && course.isCourse(), question.getLessonId(),
                    lesson == null ? "(삭제됨)" : lesson.getTitle(), question.getAuthorName(), question.getContent(),
                    question.getCreatedAt());
        });
    }

    /** 수강 중인(또는 관리자) 사용자만 항목별 질문·답글을 보고 쓸 수 있다. 질문(깊이 0)에는 답글(깊이 1)만 달 수 있다. */
    @Transactional(readOnly = true)
    public List<LessonCommentResponse> getLessonComments(final Long courseId, final Long lessonId) {
        requireLessonAccess(courseId, lessonId);
        List<CourseLessonComment> comments = lessonCommentRepository.findAllByLessonIdOrderByIdAsc(lessonId);
        Map<Long, List<CourseLessonComment>> replies = new HashMap<>();
        List<CourseLessonComment> roots = new ArrayList<>();
        for (CourseLessonComment comment : comments) {
            if (comment.getParentId() == null) {
                roots.add(comment);
            } else {
                replies.computeIfAbsent(comment.getParentId(), key -> new ArrayList<>()).add(comment);
            }
        }
        List<LessonCommentResponse> result = new ArrayList<>();
        for (CourseLessonComment root : roots) {
            List<CourseLessonComment> children = replies.getOrDefault(root.getId(), List.of()).stream()
                    .filter(child -> child.getDeletedAt() == null)
                    .toList();
            if (root.getDeletedAt() != null && children.isEmpty()) {
                continue;
            }
            result.add(LessonCommentResponse.of(root, canDeleteComment(root), canEditComment(root)));
            children.forEach(child -> result.add(LessonCommentResponse.of(child, canDeleteComment(child), canEditComment(child))));
        }
        return result;
    }

    @Transactional
    public Long createLessonComment(final Long courseId, final Long lessonId, final LessonCommentSaveRequest request) {
        requireLessonAccess(courseId, lessonId);
        LoginUser user = CurrentUser.get().orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        int depth = 0;
        CourseLessonComment parent = null;
        if (request.parentId() != null) {
            parent = lessonCommentRepository.findByIdAndLessonIdAndDeletedAtIsNull(request.parentId(), lessonId)
                    .orElseThrow(BusinessException::notFound);
            if (parent.getDepth() >= MAX_COMMENT_DEPTH) {
                throw new BusinessException(ErrorCode.FEATURE_NOT_SUPPORTED);
            }
            depth = parent.getDepth() + 1;
        }
        Long replyId = lessonCommentRepository.save(CourseLessonComment.builder()
                .lessonId(lessonId)
                .parentId(request.parentId())
                .depth(depth)
                .content(request.content())
                .userType(user.userType())
                .userId(user.id())
                .authorName(user.name())
                .build()).getId();
        boolean isSelfReply = parent != null && parent.getUserType() == user.userType() && user.id().equals(parent.getUserId());
        if (parent != null && parent.getUserType() == UserType.MEMBER && parent.getUserId() != null && !isSelfReply) {
            events.publishEvent(new CourseLessonCommentRepliedEvent(parent.getId(), replyId, courseId, lessonId, parent.getUserId()));
        }
        return replyId;
    }

    @Transactional
    public void editLessonComment(final Long courseId, final Long lessonId, final Long commentId, final LessonCommentEditRequest request) {
        findLesson(courseId, lessonId);
        CourseLessonComment comment = lessonCommentRepository.findByIdAndLessonIdAndDeletedAtIsNull(commentId, lessonId)
                .orElseThrow(BusinessException::notFound);
        if (!isLessonCommentOwner(comment)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        comment.edit(request.content());
    }

    @Transactional
    public void deleteLessonComment(final Long courseId, final Long lessonId, final Long commentId) {
        findLesson(courseId, lessonId);
        CourseLessonComment comment = lessonCommentRepository.findByIdAndLessonIdAndDeletedAtIsNull(commentId, lessonId)
                .orElseThrow(BusinessException::notFound);
        if (!canDeleteComment(comment)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        comment.delete();
    }

    private boolean canDeleteComment(final CourseLessonComment comment) {
        return CurrentUser.isAdmin() || isLessonCommentOwner(comment);
    }

    private boolean canEditComment(final CourseLessonComment comment) {
        return isLessonCommentOwner(comment);
    }

    private boolean isLessonCommentOwner(final CourseLessonComment comment) {
        return comment.getUserId() != null
                && CurrentUser.is(comment.getUserType() == null ? UserType.MEMBER : comment.getUserType(), comment.getUserId());
    }

    private CourseLesson findLesson(final Long courseId, final Long lessonId) {
        findCourse(courseId);
        return lessonRepository.findByIdAndCourseIdAndDeletedAtIsNull(lessonId, courseId).orElseThrow(BusinessException::notFound);
    }

    /** 관리자는 언제나 접근할 수 있고, 그 외에는 수강 권한이 있고(환불로 회수되지 않고) 수강 기간 안인 사용자만 접근할 수 있다. */
    private CourseLesson requireLessonAccess(final Long courseId, final Long lessonId) {
        if (CurrentUser.isAdmin()) {
            return findLesson(courseId, lessonId);
        }
        Course course = findPublic(courseId);
        gradeService.require(course.getAccessGradeId());
        CourseLesson lesson = lessonRepository.findByIdAndCourseIdAndDeletedAtIsNull(lessonId, courseId).orElseThrow(BusinessException::notFound);
        Long memberId = CurrentUser.get().filter(user -> user.userType() == UserType.MEMBER).map(LoginUser::id)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        CourseEnrollment enrollment = enrollmentRepository.findByCourseIdAndMemberId(courseId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_ENROLLED));
        if (enrollment.isRevoked()) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_REVOKED);
        }
        if (!course.accessOpen(enrollment.getCreatedAt(), enrollment.getAccessExtendedUntil(), LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_PERIOD_ENDED);
        }
        return lesson;
    }

    private int extraAttemptsOf(final Long lessonId, final Long memberId) {
        return attemptGrantRepository.findByLessonIdAndMemberId(lessonId, memberId).map(QuizAttemptGrant::getExtraAttempts).orElse(0);
    }

    private static Integer effectiveAttemptLimit(final Quiz quiz, final int extraAttempts) {
        return quiz.getRetakeLimit() == null ? null : quiz.getRetakeLimit() + extraAttempts;
    }

    private static List<Integer> shuffledIndices(final int size, final boolean shuffle) {
        List<Integer> indices = new ArrayList<>(IntStream.range(0, size).boxed().toList());
        if (shuffle) {
            Collections.shuffle(indices);
        }
        return indices;
    }

    @Transactional
    public Page<CourseResponse> getMine(final Long memberId, final Pageable pageable) {
        Page<CourseEnrollment> enrollments = enrollmentRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable);
        Map<Long, Course> courses = courseRepository.findAllById(enrollments.map(CourseEnrollment::getCourseId).toList()).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        return enrollments.map(enrollment -> {
            Course course = courses.get(enrollment.getCourseId());
            List<CourseLesson> lessons = lessonsOf(enrollment.getCourseId());
            MyCourseProgress mine = syncEnrollment(course, memberId, lessons, progressOf(memberId, lessons));
            CourseResponse summary = summary(course, lessons.size());
            LocalDateTime accessUntil = course.accessDeadline(enrollment.getCreatedAt(), enrollment.getAccessExtendedUntil());
            boolean accessOpen = !enrollment.isRevoked()
                    && course.accessOpen(enrollment.getCreatedAt(), enrollment.getAccessExtendedUntil(), LocalDateTime.now());
            return new CourseResponse(summary.id(), summary.title(), summary.description(), summary.thumbnailFileId(),
                    summary.thumbnailUrl(), summary.categoryId(), summary.sequential(), summary.accessGradeId(), course.isPublic(), course.isCourse(),
                    course.effectiveEnrollmentMode(), summary.applicationOpen(), summary.applicationStartAt(), summary.applicationEndAt(),
                    summary.accessPeriodMode(), summary.accessStartAt(), summary.accessEndAt(), summary.accessDays(),
                    accessUntil, accessOpen, course.effectiveMaxPlaybackRate(), course.effectivePreventSkipping(),
                    course.effectiveCompletionThresholdPercent(), course.isAttendanceRequired(), attendanceOk(course, enrollment),
                    course.isPaid(), course.getPrice(), course.getProductId(), course.getRefundableProgressBelowPercent(),
                    course.isTrackProgress(), course.getCompletionRate(),
                    lessons.size(), null, null, mine, null, summary.createdAt());
        });
    }

    @Transactional(readOnly = true)
    public Page<LearnerResponse> getLearners(final Long courseId, final Pageable pageable) {
        Course course = findCourse(courseId);
        List<CourseLesson> lessons = lessonsOf(courseId);
        Page<CourseEnrollment> page = enrollmentRepository.findAllByCourseIdOrderByIdDesc(courseId, pageable);
        Map<Long, String> usernames = memberRepository.findAllById(page.map(CourseEnrollment::getMemberId).toList()).stream()
                .collect(Collectors.toMap(Member::getId, Member::getUsername));
        return page.map(enrollment -> new LearnerResponse(enrollment.getMemberId(),
                usernames.getOrDefault(enrollment.getMemberId(), "(탈퇴 회원)"), enrollment.getStatus(),
                progressOf(enrollment.getMemberId(), lessons).completedCount(lessons), lessons.size(),
                enrollment.getCreatedAt(), enrollment.getCompletedAt(),
                course.accessDeadline(enrollment.getCreatedAt(), enrollment.getAccessExtendedUntil()), enrollment.getAccessExtendedUntil(),
                attendanceOk(course, enrollment)));
    }

    private Boolean attendanceOk(final Course course, final CourseEnrollment enrollment) {
        if (!course.isAttendanceRequired()) {
            return null;
        }
        if (enrollment.getCompletedAt() == null) {
            return false;
        }
        LocalDateTime deadline = course.accessDeadline(enrollment.getCreatedAt(), enrollment.getAccessExtendedUntil());
        return deadline == null || !enrollment.getCompletedAt().isAfter(deadline);
    }

    private record Progress(Set<Long> completedStreaming, Set<Long> passedLessons) {
        static final Progress EMPTY = new Progress(Set.of(), Set.of());

        boolean streamingDone(final CourseLesson lesson) {
            return lesson.getStreamingId() == null || completedStreaming.contains(lesson.getStreamingId());
        }

        boolean quizDone(final CourseLesson lesson) {
            return !lesson.hasQuiz() || passedLessons.contains(lesson.getId());
        }

        boolean completed(final CourseLesson lesson) {
            return streamingDone(lesson) && quizDone(lesson);
        }

        int completedCount(final List<CourseLesson> lessons) {
            return (int) lessons.stream().filter(this::completed).count();
        }

        /** 필수 항목을 전부 마쳤고, 전체 진도율이 과정에서 정한 수료 기준(%) 이상이면 수료다. */
        boolean meetsCompletionCriteria(final Course course, final List<CourseLesson> lessons) {
            if (lessons.isEmpty()) {
                return false;
            }
            boolean mandatoryDone = lessons.stream().filter(CourseLesson::isMandatory).allMatch(this::completed);
            return mandatoryDone && completedCount(lessons) * 100 >= lessons.size() * course.effectiveCompletionThresholdPercent();
        }
    }

    private Progress progressOf(final Long memberId, final List<CourseLesson> lessons) {
        List<Long> streamingIds = lessons.stream().map(CourseLesson::getStreamingId).filter(Objects::nonNull).distinct().toList();
        Set<Long> completedStreaming = streamingIds.isEmpty() ? Set.of()
                : streamingEnrollmentRepository.findAllByMemberIdAndStreamingIdIn(memberId, streamingIds).stream()
                .filter(StreamingEnrollment::isCompleted).map(StreamingEnrollment::getStreamingId).collect(Collectors.toSet());
        List<Long> quizLessons = lessons.stream().filter(CourseLesson::hasQuiz).map(CourseLesson::getId).toList();
        Set<Long> passed = quizLessons.isEmpty() ? Set.of() : new HashSet<>(attemptRepository.findPassedLessonIds(memberId, quizLessons));
        return new Progress(completedStreaming, passed);
    }

    private MyCourseProgress syncEnrollment(final Course course, final Long memberId, final List<CourseLesson> lessons,
                                            final Progress progress) {
        CourseEnrollment enrollment = enrollmentRepository.findByCourseIdAndMemberId(course.getId(), memberId).orElse(null);
        if (enrollment == null) {
            return null;
        }
        int completed = progress.completedCount(lessons);
        boolean wasCompleted = enrollment.getStatus() == EnrollmentStatus.COMPLETED;
        if (progress.meetsCompletionCriteria(course, lessons)) {
            enrollment.complete(LocalDateTime.now());
        }
        if (!wasCompleted && enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            events.publishEvent(new CourseCompletedEvent(course.getId(), memberId));
        }
        return new MyCourseProgress(enrollment.getStatus(), completed, enrollment.getCreatedAt(), enrollment.getCompletedAt());
    }

    private static boolean isLocked(final Course course, final List<CourseLesson> lessons, final CourseLesson lesson,
                                    final Progress progress) {
        return course.isSequential() && lessons.stream()
                .filter(previous -> previous.getSequence() < lesson.getSequence())
                .anyMatch(previous -> !progress.completed(previous));
    }

    private CourseResponse summary(final Course course, final int lessonCount) {
        return new CourseResponse(course.getId(), course.getTitle(), course.getDescription(), course.getThumbnailFileId(),
                thumbnailUrl(course), course.getCategoryId(), course.isSequential(), course.getAccessGradeId(), course.isUsing(), course.isCourse(),
                course.effectiveEnrollmentMode(), course.applicationOpen(LocalDateTime.now()), course.getApplicationStartAt(),
                course.getApplicationEndAt(), course.getAccessPeriodMode(), course.getAccessStartAt(), course.getAccessEndAt(),
                course.getAccessDays(), null, null, course.effectiveMaxPlaybackRate(), course.effectivePreventSkipping(),
                course.effectiveCompletionThresholdPercent(), course.isAttendanceRequired(), null,
                course.isPaid(), course.getPrice(), course.getProductId(), course.getRefundableProgressBelowPercent(),
                course.isTrackProgress(), course.getCompletionRate(),
                lessonCount, null, null, null, null, course.getCreatedAt());
    }

    private CourseResponse detail(final Course course, final List<CourseLesson> lessons, final Progress progress,
                                  final MyCourseProgress mine, final boolean admin, final Long memberId) {
        Map<Long, Streaming> streamingItems = streamingRepository.findAllById(lessons.stream().map(CourseLesson::getStreamingId)
                .filter(Objects::nonNull).toList()).stream().collect(Collectors.toMap(Streaming::getId, Function.identity()));
        Map<Long, Quiz> quizItems = quizRepository.findAllById(lessons.stream().map(CourseLesson::getQuizId)
                .filter(Objects::nonNull).toList()).stream().collect(Collectors.toMap(Quiz::getId, Function.identity()));
        boolean tracked = mine != null;
        Map<Long, StreamingProgress> streamingProgresses = !tracked ? Map.of() : byKey(
                streamingProgressRepository.findAllByMemberIdAndStreamingIdIn(memberId, streamingItems.keySet()),
                StreamingProgress::getStreamingId);
        Map<Long, List<QuizAttempt>> attemptsByLesson = !tracked ? Map.of() : attemptRepository
                .findAllByLessonIdInAndMemberIdOrderByIdDesc(lessons.stream().map(CourseLesson::getId).toList(), memberId).stream()
                .collect(Collectors.groupingBy(QuizAttempt::getLessonId));
        Map<Long, Integer> extraAttempts = !tracked ? Map.of() : attemptGrantRepository
                .findAllByLessonIdInAndMemberId(lessons.stream().map(CourseLesson::getId).toList(), memberId).stream()
                .collect(Collectors.toMap(QuizAttemptGrant::getLessonId, QuizAttemptGrant::getExtraAttempts));
        List<CourseMaterial> courseMaterials = materialRepository.findAllByCourseIdAndLessonIdIsNullOrderBySequenceAsc(course.getId());
        Map<Long, List<CourseMaterial>> lessonMaterials = materialRepository
                .findAllByLessonIdInOrderBySequenceAsc(lessons.stream().map(CourseLesson::getId).toList()).stream()
                .collect(Collectors.groupingBy(CourseMaterial::getLessonId));
        Map<Long, FileResponse> materialFiles = byKey(fileService.getFiles(Stream.concat(
                courseMaterials.stream(), lessonMaterials.values().stream().flatMap(List::stream))
                .map(CourseMaterial::getFileId).distinct().toList()), FileResponse::id);
        List<LessonResponse> responses = lessons.stream().map(lesson -> {
            Streaming lecture = lesson.getStreamingId() == null ? null : streamingItems.get(lesson.getStreamingId());
            Quiz quizEntity = lesson.getQuizId() == null ? null : quizItems.get(lesson.getQuizId());
            List<?> quiz = quizEntity == null ? List.of()
                    : admin ? quizEntity.getQuestions() : quizEntity.getQuestions().stream().map(PublicQuestion::of).toList();
            StreamingProgress streamingProgress = lesson.getStreamingId() == null ? null : streamingProgresses.get(lesson.getStreamingId());
            List<QuizAttempt> attempts = attemptsByLesson.getOrDefault(lesson.getId(), List.of());
            QuizAttempt displayAttempt = attempts.isEmpty() ? null
                    : quizEntity != null && quizEntity.getScorePolicy() == ScorePolicy.HIGHEST
                        ? attempts.stream().max(Comparator.comparingInt(QuizAttempt::getScore)).orElse(null)
                        : attempts.get(0);
            Integer attemptsUsed = quizEntity == null ? null : attempts.size();
            Integer attemptsAllowed = quizEntity == null ? null
                    : effectiveAttemptLimit(quizEntity, extraAttempts.getOrDefault(lesson.getId(), 0));
            return new LessonResponse(lesson.getId(), lesson.getSequence(), lesson.getTitle(), lesson.getUnitName(),
                    lesson.getStreamingId(), lecture == null ? null : lecture.getTitle(),
                    lecture == null ? null : lecture.getDurationSeconds(), lesson.getQuizId(),
                    quizEntity == null ? null : quizEntity.getTitle(), quiz, quizEntity == null ? null : quizEntity.getPassScore(),
                    tracked && lesson.getStreamingId() != null && progress.streamingDone(lesson),
                    streamingProgress == null ? null : streamingProgress.getProgressRate(),
                    streamingProgress == null ? null : streamingProgress.getLastPositionSeconds(),
                    tracked && lesson.hasQuiz() && progress.quizDone(lesson),
                    displayAttempt == null ? null : displayAttempt.getScore(),
                    tracked && progress.completed(lesson),
                    !admin && isLocked(course, lessons, lesson, progress),
                    toMaterialResponses(course.getId(), lessonMaterials.getOrDefault(lesson.getId(), List.of()), materialFiles),
                    lesson.isMandatory(), lesson.effectivePreventSkipping(course), lesson.getPreventSkippingOverride(),
                    attemptsUsed, attemptsAllowed);
        }).toList();
        CourseApplicationStatus myApplicationStatus = memberId == null ? null
                : applicationRepository.findFirstByCourseIdAndMemberIdOrderByIdDesc(course.getId(), memberId)
                        .map(CourseApplication::getStatus).orElse(null);
        LocalDateTime myAccessUntil = null;
        Boolean myAccessOpen = null;
        Boolean myAttendanceOk = null;
        if (tracked) {
            CourseEnrollment enrollment = enrollmentRepository.findByCourseIdAndMemberId(course.getId(), memberId).orElse(null);
            if (enrollment != null) {
                myAccessUntil = course.accessDeadline(enrollment.getCreatedAt(), enrollment.getAccessExtendedUntil());
                myAccessOpen = !enrollment.isRevoked()
                        && course.accessOpen(enrollment.getCreatedAt(), enrollment.getAccessExtendedUntil(), LocalDateTime.now());
                myAttendanceOk = attendanceOk(course, enrollment);
            }
        }
        return new CourseResponse(course.getId(), course.getTitle(), course.getDescription(), course.getThumbnailFileId(),
                thumbnailUrl(course), course.getCategoryId(), course.isSequential(), course.getAccessGradeId(), course.isUsing(), course.isCourse(),
                course.effectiveEnrollmentMode(), course.applicationOpen(LocalDateTime.now()), course.getApplicationStartAt(),
                course.getApplicationEndAt(), course.getAccessPeriodMode(), course.getAccessStartAt(), course.getAccessEndAt(),
                course.getAccessDays(), myAccessUntil, myAccessOpen, course.effectiveMaxPlaybackRate(), course.effectivePreventSkipping(),
                course.effectiveCompletionThresholdPercent(), course.isAttendanceRequired(), myAttendanceOk,
                course.isPaid(), course.getPrice(), course.getProductId(), course.getRefundableProgressBelowPercent(),
                course.isTrackProgress(), course.getCompletionRate(),
                lessons.size(), responses, toMaterialResponses(course.getId(), courseMaterials, materialFiles), mine,
                myApplicationStatus, course.getCreatedAt());
    }

    private static List<MaterialResponse> toMaterialResponses(final Long courseId, final List<CourseMaterial> materials,
                                                               final Map<Long, FileResponse> files) {
        return materials.stream().map(material -> {
            FileResponse file = files.get(material.getFileId());
            return new MaterialResponse(material.getId(), file == null ? "(삭제된 파일)" : file.originalName(),
                    file == null ? null : file.mimeType(), file == null ? null : file.size(),
                    "/api/courses/" + courseId + "/materials/" + material.getId() + "/download");
        }).toList();
    }

    /** 기존 항목은 ID로 찾아 고쳐서, 수강생의 퀴즈 통과 기록이 이어지게 한다. */
    private void syncLessons(final Long courseId, final List<LessonRequest> requests) {
        Map<Long, CourseLesson> existing = lessonsOf(courseId).stream()
                .collect(Collectors.toMap(CourseLesson::getId, Function.identity()));
        for (int i = 0; i < requests.size(); i++) {
            LessonRequest request = requests.get(i);
            CourseLesson lesson = request.id() == null ? null : existing.remove(request.id());
            if (lesson == null) {
                lesson = lessonRepository.save(CourseLesson.builder().courseId(courseId).sequence(i + 1).title(request.title().trim())
                        .unitName(blankToNull(request.unitName())).streamingId(request.streamingId()).quizId(request.quizId())
                        .mandatory(request.mandatory()).preventSkippingOverride(request.preventSkippingOverride()).build());
            } else {
                lesson.update(i + 1, request.title().trim(), blankToNull(request.unitName()), request.streamingId(), request.quizId(),
                        request.mandatory(), request.preventSkippingOverride());
            }
            syncMaterials(courseId, lesson.getId(), request.materialFileIdsOrEmpty());
        }
        existing.values().forEach(lesson -> {
            materialRepository.deleteAllByLessonId(lesson.getId());
            lesson.delete();
        });
    }

    private void syncMaterials(final Long courseId, final Long lessonId, final List<Long> fileIds) {
        if (lessonId == null) {
            materialRepository.deleteAllByCourseIdAndLessonIdIsNull(courseId);
        } else {
            materialRepository.deleteAllByLessonId(lessonId);
        }
        if (fileIds.isEmpty()) {
            return;
        }
        fileService.requireFilesExist(fileIds);
        fileService.protect(fileIds);
        for (int i = 0; i < fileIds.size(); i++) {
            materialRepository.save(CourseMaterial.builder().courseId(courseId).lessonId(lessonId).fileId(fileIds.get(i)).sequence(i).build());
        }
    }

    private static String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validate(final CourseSaveRequest request) {
        for (LessonRequest lesson : request.lessons()) {
            if (lesson.streamingId() == null && lesson.quizId() == null) {
                throw new BusinessException(ErrorCode.INVALID_LESSON);
            }
            if (lesson.streamingId() != null
                    && streamingRepository.findById(lesson.streamingId()).filter(streaming -> streaming.getDeletedAt() == null).isEmpty()) {
                throw BusinessException.notFound();
            }
            if (lesson.quizId() != null) {
                if (!request.isCourse()) {
                    throw new BusinessException(ErrorCode.QUIZ_REQUIRES_COURSE_MODE);
                }
                if (quizRepository.findById(lesson.quizId()).filter(quiz -> quiz.getDeletedAt() == null).isEmpty()) {
                    throw BusinessException.notFound();
                }
            }
        }
        if (request.thumbnailFileId() != null) {
            fileService.requireFilesExist(List.of(request.thumbnailFileId()));
        }
        if (request.applicationStartAt() != null && request.applicationEndAt() != null
                && !request.applicationStartAt().isBefore(request.applicationEndAt())) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
        if (request.isCourse()) {
            AccessPeriodMode periodMode = request.accessPeriodModeOrDefault();
            if (periodMode == AccessPeriodMode.FIXED) {
                if (request.accessStartAt() == null || request.accessEndAt() == null) {
                    throw new BusinessException(ErrorCode.COURSE_ACCESS_PERIOD_REQUIRED);
                }
                if (!request.accessStartAt().isBefore(request.accessEndAt())) {
                    throw new BusinessException(ErrorCode.INVALID_PERIOD);
                }
            } else if (periodMode == AccessPeriodMode.DAYS_FROM_ENROLLMENT && request.accessDays() == null) {
                throw new BusinessException(ErrorCode.COURSE_ACCESS_DAYS_REQUIRED);
            }
        }
        if (request.paid() && (request.price() == null || request.price().signum() <= 0)) {
            throw new BusinessException(ErrorCode.COURSE_PRICE_REQUIRED);
        }
    }

    private List<CourseLesson> lessonsOf(final Long courseId) {
        return lessonRepository.findAllByCourseIdAndDeletedAtIsNullOrderBySequenceAsc(courseId);
    }

    private static String thumbnailUrl(final Course course) {
        return course.getThumbnailFileId() == null ? null : "/api/files/" + course.getThumbnailFileId() + "/view";
    }

    private Course findCourse(final Long id) {
        return courseRepository.findByIdAndSiteIdAndDeletedAtIsNull(id, SiteContextHolder.currentSiteId()).orElseThrow(BusinessException::notFound);
    }

    private Course findPublic(final Long id) {
        return courseRepository.findByIdAndSiteIdAndDeletedAtIsNull(id, SiteContextHolder.currentSiteId()).filter(Course::isPublic).orElseThrow(BusinessException::notFound);
    }

    private static <T> Map<Long, T> byKey(final Collection<T> items, final Function<T, Long> key) {
        return items.stream().collect(Collectors.toMap(key, Function.identity(), (first, second) -> first));
    }
}

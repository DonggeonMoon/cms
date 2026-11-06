package com.dgmoonlabs.cms.domain.course.service;

import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.common.grade.service.GradeService;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.domain.course.dto.CourseDtos.*;
import com.dgmoonlabs.cms.domain.course.entity.*;
import com.dgmoonlabs.cms.domain.course.repository.CourseEnrollmentRepository;
import com.dgmoonlabs.cms.domain.course.repository.CourseLessonRepository;
import com.dgmoonlabs.cms.domain.course.repository.CourseRepository;
import com.dgmoonlabs.cms.domain.course.repository.QuizAttemptRepository;
import com.dgmoonlabs.cms.domain.streaming.entity.Streaming;
import com.dgmoonlabs.cms.domain.streaming.entity.StreamingEnrollment;
import com.dgmoonlabs.cms.domain.streaming.repository.StreamingEnrollmentRepository;
import com.dgmoonlabs.cms.domain.streaming.repository.StreamingRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import com.dgmoonlabs.cms.global.site.SiteContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CourseService {
    private static final int DEFAULT_PASS_SCORE = 60;

    private final CourseRepository courseRepository;
    private final CourseLessonRepository lessonRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final QuizAttemptRepository attemptRepository;
    private final StreamingRepository streamingRepository;
    private final StreamingEnrollmentRepository streamingEnrollmentRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;
    private final GradeService gradeService;

    @Transactional(readOnly = true)
    public Page<CourseResponse> getCourses(final String keyword, final Pageable pageable) {
        String term = keyword == null ? "" : keyword;
        Long siteId = SiteContextHolder.currentSiteId();
        Page<Course> courses = CurrentUser.isAdmin()
                ? courseRepository.findAllBySiteIdAndDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdDesc(siteId, term, pageable)
                : courseRepository.findAllByIsUsingTrueAndSiteIdAndDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdDesc(siteId, term, pageable);
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
        return detail(course, lessons, progress, mine, admin);
    }

    @Transactional
    public Long createCourse(final CourseSaveRequest request) {
        validate(request);
        Course course = courseRepository.save(Course.builder()
                .siteId(SiteContextHolder.currentSiteId())
                .title(request.title().trim())
                .description(request.description())
                .thumbnailFileId(request.thumbnailFileId())
                .sequential(request.sequential())
                .accessGradeId(gradeService.validGradeId(request.accessGradeId()))
                .isUsing(request.isUsing())
                .build());
        syncLessons(course.getId(), request.lessons());
        return course.getId();
    }

    @Transactional
    public void updateCourse(final Long id, final CourseSaveRequest request) {
        validate(request);
        findCourse(id).update(request.title().trim(), request.description(), request.thumbnailFileId(), request.sequential(),
                gradeService.validGradeId(request.accessGradeId()), request.isUsing());
        syncLessons(id, request.lessons());
    }

    @Transactional
    public void deleteCourse(final Long id) {
        findCourse(id).delete();
        lessonsOf(id).forEach(CourseLesson::delete);
    }

    @Transactional
    public CourseResponse enroll(final Long courseId, final LoginUser member) {
        Course course = findPublic(courseId);
        gradeService.require(course.getAccessGradeId());
        enrollmentRepository.findByCourseIdAndMemberId(courseId, member.id())
                .orElseGet(() -> enrollmentRepository.save(CourseEnrollment.builder().courseId(courseId).memberId(member.id()).build()));
        return getCourse(courseId);
    }

    @Transactional
    public QuizResult submitQuiz(final Long courseId, final Long lessonId, final LoginUser member, final QuizSubmitRequest request) {
        Course course = findPublic(courseId);
        gradeService.require(course.getAccessGradeId());
        enrollmentRepository.findByCourseIdAndMemberId(courseId, member.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_ENROLLED));
        List<CourseLesson> lessons = lessonsOf(courseId);
        CourseLesson lesson = lessons.stream().filter(candidate -> candidate.getId().equals(lessonId)).findFirst()
                .filter(CourseLesson::hasQuiz)
                .orElseThrow(BusinessException::notFound);
        Progress before = progressOf(member.id(), lessons);
        if (isLocked(course, lessons, lesson, before)) {
            throw new BusinessException(ErrorCode.LESSON_LOCKED);
        }
        List<QuizQuestion> quiz = lesson.getQuiz();
        if (request.answers().size() != quiz.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        List<Boolean> correct = IntStream.range(0, quiz.size())
                .mapToObj(i -> quiz.get(i).answerIndex().equals(request.answers().get(i))).toList();
        int score = (int) Math.round(correct.stream().filter(Boolean::booleanValue).count() * 100.0 / quiz.size());
        boolean passed = score >= lesson.getPassScore();
        attemptRepository.save(QuizAttempt.builder()
                .lessonId(lessonId).memberId(member.id()).answers(request.answers()).score(score).passed(passed).build());
        MyCourseProgress mine = syncEnrollment(course, member.id(), lessons, progressOf(member.id(), lessons));
        return new QuizResult(score, lesson.getPassScore(), passed, correct,
                attemptRepository.countByLessonIdAndMemberId(lessonId, member.id()), mine);
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
            return new CourseResponse(summary.id(), summary.title(), summary.description(), summary.thumbnailFileId(),
                    summary.thumbnailUrl(), summary.sequential(), summary.accessGradeId(), course.isPublic(), lessons.size(),
                    null, mine, summary.createdAt());
        });
    }

    @Transactional(readOnly = true)
    public Page<LearnerResponse> getLearners(final Long courseId, final Pageable pageable) {
        findCourse(courseId);
        List<CourseLesson> lessons = lessonsOf(courseId);
        Page<CourseEnrollment> page = enrollmentRepository.findAllByCourseIdOrderByIdDesc(courseId, pageable);
        Map<Long, String> usernames = memberRepository.findAllById(page.map(CourseEnrollment::getMemberId).toList()).stream()
                .collect(Collectors.toMap(Member::getId, Member::getUsername));
        return page.map(enrollment -> new LearnerResponse(enrollment.getMemberId(),
                usernames.getOrDefault(enrollment.getMemberId(), "(탈퇴 회원)"), enrollment.getStatus(),
                progressOf(enrollment.getMemberId(), lessons).completedCount(lessons), lessons.size(),
                enrollment.getCreatedAt(), enrollment.getCompletedAt()));
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
        if (!lessons.isEmpty() && completed == lessons.size()) {
            enrollment.complete(LocalDateTime.now());
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
                thumbnailUrl(course), course.isSequential(), course.getAccessGradeId(), course.isUsing(), lessonCount, null, null,
                course.getCreatedAt());
    }

    private CourseResponse detail(final Course course, final List<CourseLesson> lessons, final Progress progress,
                                  final MyCourseProgress mine, final boolean admin) {
        Map<Long, Streaming> streamingItems = streamingRepository.findAllById(lessons.stream().map(CourseLesson::getStreamingId)
                .filter(Objects::nonNull).toList()).stream().collect(Collectors.toMap(Streaming::getId, Function.identity()));
        List<LessonResponse> responses = lessons.stream().map(lesson -> {
            Streaming lecture = lesson.getStreamingId() == null ? null : streamingItems.get(lesson.getStreamingId());
            List<?> quiz = !lesson.hasQuiz() ? List.of()
                    : admin ? lesson.getQuiz() : lesson.getQuiz().stream().map(PublicQuestion::of).toList();
            boolean tracked = mine != null;
            return new LessonResponse(lesson.getId(), lesson.getSequence(), lesson.getTitle(), lesson.getStreamingId(),
                    lecture == null ? null : lecture.getTitle(), lecture == null ? null : lecture.getDurationSeconds(),
                    quiz, lesson.getPassScore(), tracked && lesson.getStreamingId() != null && progress.streamingDone(lesson),
                    tracked && lesson.hasQuiz() && progress.quizDone(lesson), tracked && progress.completed(lesson),
                    !admin && isLocked(course, lessons, lesson, progress));
        }).toList();
        return new CourseResponse(course.getId(), course.getTitle(), course.getDescription(), course.getThumbnailFileId(),
                thumbnailUrl(course), course.isSequential(), course.getAccessGradeId(), course.isUsing(), lessons.size(),
                responses, mine, course.getCreatedAt());
    }

    /** 기존 차시는 ID로 찾아 고쳐서, 수강생의 퀴즈 통과 기록이 이어지게 한다. */
    private void syncLessons(final Long courseId, final List<LessonRequest> requests) {
        Map<Long, CourseLesson> existing = lessonsOf(courseId).stream()
                .collect(Collectors.toMap(CourseLesson::getId, Function.identity()));
        for (int i = 0; i < requests.size(); i++) {
            LessonRequest request = requests.get(i);
            List<QuizQuestion> quiz = request.quizOrEmpty();
            Integer passScore = quiz.isEmpty() ? null : request.passScore() == null ? DEFAULT_PASS_SCORE : request.passScore();
            CourseLesson lesson = request.id() == null ? null : existing.remove(request.id());
            if (lesson == null) {
                lessonRepository.save(CourseLesson.builder().courseId(courseId).sequence(i + 1).title(request.title().trim())
                        .streamingId(request.streamingId()).quiz(quiz.isEmpty() ? null : quiz).passScore(passScore).build());
            } else {
                lesson.update(i + 1, request.title().trim(), request.streamingId(), quiz, passScore);
            }
        }
        existing.values().forEach(CourseLesson::delete);
    }

    private void validate(final CourseSaveRequest request) {
        for (LessonRequest lesson : request.lessons()) {
            if (lesson.streamingId() == null && lesson.quizOrEmpty().isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_LESSON);
            }
            if (lesson.streamingId() != null
                    && streamingRepository.findById(lesson.streamingId()).filter(streaming -> streaming.getDeletedAt() == null).isEmpty()) {
                throw BusinessException.notFound();
            }
            if (lesson.quizOrEmpty().stream().anyMatch(question -> question.answerIndex() >= question.options().size())) {
                throw new BusinessException(ErrorCode.INVALID_LESSON);
            }
        }
        if (request.thumbnailFileId() != null) {
            fileService.requireFilesExist(List.of(request.thumbnailFileId()));
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
}

package com.dgmoonlabs.cms.domain.application.service;

import com.dgmoonlabs.cms.domain.application.constant.FormFieldType;
import com.dgmoonlabs.cms.domain.application.constant.SelectionMethod;
import com.dgmoonlabs.cms.domain.application.constant.SubmissionStatus;
import com.dgmoonlabs.cms.domain.application.dto.ApplicationDtos.*;
import com.dgmoonlabs.cms.domain.application.entity.ApplicationForm;
import com.dgmoonlabs.cms.domain.application.entity.ApplicationSubmission;
import com.dgmoonlabs.cms.domain.application.entity.ContestScore;
import com.dgmoonlabs.cms.domain.application.entity.FormField;
import com.dgmoonlabs.cms.domain.application.repository.ApplicationFormRepository;
import com.dgmoonlabs.cms.domain.application.repository.ApplicationSubmissionRepository;
import com.dgmoonlabs.cms.domain.application.repository.ContestScoreRepository;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderCreateRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderResponse;
import com.dgmoonlabs.cms.domain.business.order.service.OrderService;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.service.RegexService;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.error.ErrorResponse.FieldError;
import com.dgmoonlabs.cms.global.error.FieldValidationException;
import com.dgmoonlabs.cms.global.html.HtmlSanitizer;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private static final int MAX_TEXT = 5000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ApplicationFormRepository formRepository;
    private final ApplicationSubmissionRepository submissionRepository;
    private final ContestScoreRepository contestScoreRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;
    private final RegexService regexService;
    private final HtmlSanitizer htmlSanitizer;
    private final JsonMapper jsonMapper;
    private final OrderService orderService;

    @Transactional(readOnly = true)
    public Page<FormResponse> getForms(final String keyword, final Pageable pageable) {
        String term = keyword == null ? "" : keyword;
        Page<ApplicationForm> forms = CurrentUser.isAdmin()
                ? formRepository.findAllByDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdDesc(term, pageable)
                : formRepository.findAllByIsEnabledTrueAndDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdDesc(term, pageable);
        return forms.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public FormResponse getForm(final Long id) {
        ApplicationForm form = findForm(id);
        if (!form.isEnabled() && !CurrentUser.isAdmin()) {
            throw BusinessException.notFound();
        }
        return toResponse(form);
    }

    @Transactional
    public Long createForm(final FormSaveRequest request) {
        Integer winnerCount = requireWinnerCount(request);
        String judgeMemberIds = requireJudges(request);
        String html = htmlSanitizer.sanitize(request.contentHtml());
        return formRepository.save(ApplicationForm.builder()
                .title(request.title().trim())
                .contentHtml(html)
                .fields(writeFields(request.fields()))
                .startDateTime(request.startDateTime())
                .endDateTime(request.endDateTime())
                .maximumSubmissions(request.maximumSubmissions())
                .allowMultiple(request.allowMultiple())
                .isEnabled(request.isEnabled())
                .selectionMethod(request.selectionMethodOrDefault())
                .winnerCount(winnerCount)
                .admissionFeeProductId(request.admissionFeeProductId())
                .judgeMemberIds(judgeMemberIds)
                .build()).getId();
    }

    @Transactional
    public void updateForm(final Long id, final FormSaveRequest request) {
        Integer winnerCount = requireWinnerCount(request);
        String judgeMemberIds = requireJudges(request);
        findForm(id).update(request.title().trim(), htmlSanitizer.sanitize(request.contentHtml()), writeFields(request.fields()),
                request.startDateTime(), request.endDateTime(), request.maximumSubmissions(), request.allowMultiple(),
                request.isEnabled(), request.selectionMethodOrDefault(), winnerCount, request.admissionFeeProductId(), judgeMemberIds);
    }

    @Transactional
    public void deleteForm(final Long id) {
        findForm(id).delete();
    }

    @Transactional
    public DrawResponse draw(final Long formId) {
        ApplicationForm form = formRepository.findForUpdate(formId).orElseThrow(BusinessException::notFound);
        LocalDateTime now = LocalDateTime.now();
        form.markDrawn(now);
        List<ApplicationSubmission> candidates = new ArrayList<>(submissionRepository.findAllByFormIdAndStatusIn(formId,
                EnumSet.of(SubmissionStatus.RECEIVED, SubmissionStatus.REVIEWING)));
        Collections.shuffle(candidates, RANDOM);
        int winners = Math.min(form.getWinnerCount(), candidates.size());
        for (int i = 0; i < candidates.size(); i++) {
            candidates.get(i).drawResult(i < winners);
        }
        return new DrawResponse(candidates.size(), winners, now);
    }

    @Transactional
    public SubmissionResponse submit(final LoginUser member, final Long formId, final SubmitRequest request) {
        ApplicationForm form = formRepository.findForUpdate(formId).orElseThrow(BusinessException::notFound);
        if (!form.isOpenAt(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_OPEN);
        }
        if (!form.isAllowMultiple()
                && submissionRepository.existsByFormIdAndMemberIdAndStatusNot(formId, member.id(), SubmissionStatus.WITHDRAWN)) {
            throw new BusinessException(ErrorCode.ALREADY_APPLIED);
        }
        if (form.getMaximumSubmissions() != null
                && submissionRepository.countActive(formId, SubmissionStatus.WITHDRAWN) >= form.getMaximumSubmissions()) {
            throw new BusinessException(ErrorCode.APPLICATION_FULL);
        }
        Map<String, Object> answers = validateAnswers(readFields(form), request.answers());
        ApplicationSubmission submission = submissionRepository.save(ApplicationSubmission.builder()
                .formId(formId)
                .formTitle(form.getTitle())
                .memberId(member.id())
                .answers(jsonMapper.writeValueAsString(answers))
                .build());
        return SubmissionResponse.of(submission, answers, member.username(), false);
    }

    @Transactional(readOnly = true)
    public Page<SubmissionResponse> getMine(final Long memberId, final Pageable pageable) {
        return submissionRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable)
                .map(submission -> SubmissionResponse.of(submission, readAnswers(submission), null, false));
    }

    @Transactional
    public SubmissionResponse withdrawMine(final Long memberId, final Long id) {
        ApplicationSubmission submission = submissionRepository.findByIdAndMemberId(id, memberId).orElseThrow(BusinessException::notFound);
        submission.withdraw();
        return SubmissionResponse.of(submission, readAnswers(submission), null, false);
    }

    @Transactional(readOnly = true)
    public Page<SubmissionResponse> getSubmissions(final Long formId, final SubmissionStatus status, final Pageable pageable) {
        ApplicationForm form = findForm(formId);
        Page<ApplicationSubmission> page = submissionRepository.search(formId, status, pageable);
        Map<Long, String> usernames = memberRepository.findAllById(page.map(ApplicationSubmission::getMemberId).toList()).stream()
                .collect(Collectors.toMap(Member::getId, Member::getUsername));
        Map<Long, List<ContestScore>> scoresBySubmission = form.getSelectionMethod() != SelectionMethod.CONTEST ? Map.of()
                : contestScoreRepository.findAllBySubmissionIdIn(page.map(ApplicationSubmission::getId).toList())
                .stream().collect(Collectors.groupingBy(ContestScore::getSubmissionId));
        return page.map(submission -> {
            List<ContestScore> scores = scoresBySubmission.getOrDefault(submission.getId(), List.of());
            BigDecimal average = averageOf(scores);
            return SubmissionResponse.of(submission, readAnswers(submission),
                    usernames.getOrDefault(submission.getMemberId(), "(탈퇴 회원)"), true, average, scores.isEmpty() ? null : scores.size());
        });
    }

    @Transactional(readOnly = true)
    public List<ScoreDetail> getScores(final Long submissionId) {
        Map<Long, String> usernames = memberRepository.findAllById(
                contestScoreRepository.findAllBySubmissionId(submissionId).stream().map(ContestScore::getJudgeMemberId).toList())
                .stream().collect(Collectors.toMap(Member::getId, Member::getUsername));
        return contestScoreRepository.findAllBySubmissionId(submissionId).stream()
                .map(score -> new ScoreDetail(score.getJudgeMemberId(), usernames.getOrDefault(score.getJudgeMemberId(), "(탈퇴 회원)"),
                        score.getScore(), score.getComment(), score.getModifiedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<JudgeSubmissionResponse> getJudgeSubmissions(final Long judgeMemberId, final Long formId, final Pageable pageable) {
        ApplicationForm form = findForm(formId);
        if (!judgeIdsOf(form).contains(judgeMemberId)) {
            throw new BusinessException(ErrorCode.NOT_JUDGE);
        }
        Page<ApplicationSubmission> page = submissionRepository.search(formId, null, pageable);
        Map<Long, ContestScore> myScores = contestScoreRepository.findAllBySubmissionIdIn(page.map(ApplicationSubmission::getId).toList())
                .stream().filter(score -> score.getJudgeMemberId().equals(judgeMemberId))
                .collect(Collectors.toMap(ContestScore::getSubmissionId, score -> score));
        return page.map(submission -> {
            ContestScore mine = myScores.get(submission.getId());
            return new JudgeSubmissionResponse(submission.getId(), submission.getFormId(), readAnswers(submission),
                    mine == null ? null : mine.getScore(), mine == null ? null : mine.getComment(), submission.getCreatedAt());
        });
    }

    @Transactional
    public ScoreResponse submitScore(final Long judgeMemberId, final Long submissionId, final ScoreRequest request) {
        ApplicationSubmission submission = submissionRepository.findById(submissionId).orElseThrow(BusinessException::notFound);
        ApplicationForm form = findForm(submission.getFormId());
        if (form.getSelectionMethod() != SelectionMethod.CONTEST) {
            throw new BusinessException(ErrorCode.NOT_CONTEST);
        }
        if (!judgeIdsOf(form).contains(judgeMemberId)) {
            throw new BusinessException(ErrorCode.NOT_JUDGE);
        }
        if (submission.getStatus() == SubmissionStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        String comment = blankToNull(request.comment());
        ContestScore score = contestScoreRepository.findBySubmissionIdAndJudgeMemberId(submissionId, judgeMemberId)
                .map(existing -> {
                    existing.update(request.score(), comment);
                    return existing;
                })
                .orElseGet(() -> contestScoreRepository.save(ContestScore.builder()
                        .submissionId(submissionId).judgeMemberId(judgeMemberId).score(request.score()).comment(comment).build()));
        return new ScoreResponse(submissionId, score.getScore(), score.getComment(), score.getModifiedAt());
    }

    @Transactional
    public DrawResponse finalizeContest(final Long formId) {
        ApplicationForm form = formRepository.findForUpdate(formId).orElseThrow(BusinessException::notFound);
        LocalDateTime now = LocalDateTime.now();
        form.markContestFinalized(now);
        List<ApplicationSubmission> candidates = new ArrayList<>(submissionRepository.findAllByFormIdAndStatusIn(formId,
                EnumSet.of(SubmissionStatus.RECEIVED, SubmissionStatus.REVIEWING)));
        Map<Long, List<ContestScore>> scoresBySubmission = contestScoreRepository
                .findAllBySubmissionIdIn(candidates.stream().map(ApplicationSubmission::getId).toList())
                .stream().collect(Collectors.groupingBy(ContestScore::getSubmissionId));
        candidates.sort(Comparator.comparing(
                (ApplicationSubmission submission) -> averageOf(scoresBySubmission.getOrDefault(submission.getId(), List.of())),
                Comparator.nullsFirst(Comparator.naturalOrder())).reversed().thenComparing(ApplicationSubmission::getId));
        int winners = Math.min(form.getWinnerCount(), candidates.size());
        for (int i = 0; i < candidates.size(); i++) {
            boolean won = i < winners;
            candidates.get(i).review(won ? SubmissionStatus.ACCEPTED : SubmissionStatus.REJECTED,
                    won ? "공모전에 선정되었습니다." : "아쉽게도 선정되지 않았습니다.", candidates.get(i).getAdminMemo());
        }
        return new DrawResponse(candidates.size(), winners, now);
    }

    private static BigDecimal averageOf(final List<ContestScore> scores) {
        if (scores.isEmpty()) {
            return null;
        }
        BigDecimal sum = scores.stream().map(ContestScore::getScore).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public SubmissionResponse review(final Long submissionId, final ReviewRequest request) {
        ApplicationSubmission submission = submissionRepository.findById(submissionId).orElseThrow(BusinessException::notFound);
        submission.review(request.status(), blankToNull(request.resultMessage()), blankToNull(request.adminMemo()));
        return SubmissionResponse.of(submission, readAnswers(submission), null, true);
    }

    /** 합격한 신청자가 입학금·입회비 등을 결제한다. 가격은 연결한 상품을 그대로 따른다. */
    @Transactional
    public OrderResponse payAdmissionFee(final Long memberId, final Long submissionId) {
        ApplicationSubmission submission = submissionRepository.findByIdAndMemberId(submissionId, memberId).orElseThrow(BusinessException::notFound);
        if (submission.getStatus() != SubmissionStatus.ACCEPTED) {
            throw new BusinessException(ErrorCode.SUBMISSION_NOT_ACCEPTED);
        }
        if (submission.getAdmissionOrderId() != null) {
            throw new BusinessException(ErrorCode.ADMISSION_ALREADY_ORDERED);
        }
        ApplicationForm form = findForm(submission.getFormId());
        if (form.getAdmissionFeeProductId() == null) {
            throw new BusinessException(ErrorCode.ADMISSION_FEE_NOT_CONFIGURED);
        }
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(BusinessException::notFound);
        OrderResponse order = orderService.createOrder(memberId, new OrderCreateRequest(form.getAdmissionFeeProductId(), null, 1,
                null, member.getName(), phoneOf(member), null, null, null, null, form.getTitle(), null, null, null, null, null));
        submission.linkAdmissionOrder(order.id());
        return order;
    }

    private static String phoneOf(final Member member) {
        return member.getMobilePhoneNumber() != null ? member.getMobilePhoneNumber() : member.getTelephoneNumber();
    }

    private Map<String, Object> validateAnswers(final List<FormField> fields, final Map<String, Object> input) {
        Map<String, Object> answers = new LinkedHashMap<>();
        List<FieldError> errors = new ArrayList<>();
        for (FormField field : fields) {
            Object raw = input.get(field.key());
            if (isEmpty(raw)) {
                if (field.required()) {
                    errors.add(new FieldError(field.key(), field.label() + "을(를) 입력해 주세요."));
                }
                continue;
            }
            Object value = normalize(field, raw);
            if (value == null) {
                errors.add(new FieldError(field.key(), field.label() + " 형식이 올바르지 않습니다."));
                continue;
            }
            Optional<String> violation = switch (field.type()) {
                case EMAIL -> regexService.violation(RegexTarget.EMAIL, (String) value);
                case PHONE -> regexService.violation(RegexTarget.PHONE_NUMBER, (String) value);
                default -> Optional.empty();
            };
            if (violation.isPresent()) {
                errors.add(new FieldError(field.key(), violation.get()));
                continue;
            }
            answers.put(field.key(), value);
        }
        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
        List<Long> fileIds = fields.stream().filter(field -> field.type() == FormFieldType.FILE && answers.containsKey(field.key()))
                .map(field -> (Long) answers.get(field.key())).toList();
        if (!fileIds.isEmpty()) {
            fileService.requireFilesExist(fileIds);
        }
        return answers;
    }

    private static Object normalize(final FormField field, final Object raw) {
        try {
            return switch (field.type()) {
                case TEXT, TEXTAREA, EMAIL, PHONE -> raw instanceof String text && text.length() <= MAX_TEXT ? text.trim() : null;
                case NUMBER -> new BigDecimal(String.valueOf(raw).trim());
                case DATE -> LocalDate.parse(String.valueOf(raw)).toString();
                case SELECT, RADIO -> field.optionsOrEmpty().contains(String.valueOf(raw)) ? String.valueOf(raw) : null;
                case CHECKBOX -> {
                    if (field.optionsOrEmpty().isEmpty()) {
                        yield Boolean.parseBoolean(String.valueOf(raw)) ? Boolean.TRUE : null;
                    }
                    List<String> picked = raw instanceof Collection<?> values ? values.stream().map(String::valueOf).toList() : List.of(String.valueOf(raw));
                    yield field.optionsOrEmpty().containsAll(picked) ? picked : null;
                }
                case FILE -> Long.valueOf(String.valueOf(raw));
            };
        } catch (NumberFormatException | DateTimeParseException e) {
            return null;
        }
    }

    private static boolean isEmpty(final Object value) {
        return value == null || (value instanceof String text && text.isBlank()) || (value instanceof Collection<?> c && c.isEmpty())
                || Boolean.FALSE.equals(value);
    }

    private String writeFields(final List<FormField> fields) {
        Set<String> keys = new HashSet<>();
        for (FormField field : fields) {
            boolean choice = field.type() == FormFieldType.SELECT || field.type() == FormFieldType.RADIO;
            if (!keys.add(field.key()) || (choice && field.optionsOrEmpty().isEmpty())) {
                throw new BusinessException(ErrorCode.INVALID_FORM_FIELDS);
            }
        }
        return jsonMapper.writeValueAsString(fields);
    }

    private List<FormField> readFields(final ApplicationForm form) {
        return jsonMapper.readValue(form.getFields(), new TypeReference<List<FormField>>() {
        });
    }

    private Map<String, Object> readAnswers(final ApplicationSubmission submission) {
        return jsonMapper.readValue(submission.getAnswers(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
    }

    private FormResponse toResponse(final ApplicationForm form) {
        boolean applied = CurrentUser.isMember() && CurrentUser.get()
                .map(user -> submissionRepository.existsByFormIdAndMemberIdAndStatusNot(form.getId(), user.id(), SubmissionStatus.WITHDRAWN))
                .orElse(false);
        List<Long> judgeIds = judgeIdsOf(form);
        boolean isJudge = CurrentUser.isMember() && CurrentUser.get().map(user -> judgeIds.contains(user.id())).orElse(false);
        List<String> judgeUsernames = judgeIds.isEmpty() ? List.of()
                : memberRepository.findAllById(judgeIds).stream().map(Member::getUsername).toList();
        return FormResponse.of(form, readFields(form), submissionRepository.countActive(form.getId(), SubmissionStatus.WITHDRAWN),
                form.isOpenAt(LocalDateTime.now()), applied, judgeUsernames, isJudge);
    }

    private ApplicationForm findForm(final Long id) {
        return formRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private static Integer requireWinnerCount(final FormSaveRequest request) {
        SelectionMethod method = request.selectionMethodOrDefault();
        if (method != SelectionMethod.LOTTERY && method != SelectionMethod.CONTEST) {
            return null;
        }
        if (request.winnerCount() == null) {
            throw new BusinessException(ErrorCode.WINNER_COUNT_REQUIRED);
        }
        return request.winnerCount();
    }

    private String requireJudges(final FormSaveRequest request) {
        if (request.selectionMethodOrDefault() != SelectionMethod.CONTEST) {
            return null;
        }
        List<String> usernames = request.judgeUsernames();
        if (usernames == null || usernames.isEmpty()) {
            throw new BusinessException(ErrorCode.JUDGES_REQUIRED);
        }
        List<Long> judgeIds = usernames.stream().distinct().map(username ->
                memberRepository.findByUsernameAndDeletedAtIsNull(username).orElseThrow(() -> new BusinessException(ErrorCode.JUDGE_NOT_FOUND))
                        .getId()).toList();
        return jsonMapper.writeValueAsString(judgeIds);
    }

    private List<Long> judgeIdsOf(final ApplicationForm form) {
        if (form.getJudgeMemberIds() == null) {
            return List.of();
        }
        return jsonMapper.readValue(form.getJudgeMemberIds(), new TypeReference<List<Long>>() {
        });
    }

    private static String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

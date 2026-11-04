package com.dgmoonlabs.cms.domain.application.service;

import com.dgmoonlabs.cms.domain.application.constant.FormFieldType;
import com.dgmoonlabs.cms.domain.application.constant.SubmissionStatus;
import com.dgmoonlabs.cms.domain.application.dto.ApplicationDtos.*;
import com.dgmoonlabs.cms.domain.application.entity.ApplicationForm;
import com.dgmoonlabs.cms.domain.application.entity.ApplicationSubmission;
import com.dgmoonlabs.cms.domain.application.entity.FormField;
import com.dgmoonlabs.cms.domain.application.repository.ApplicationFormRepository;
import com.dgmoonlabs.cms.domain.application.repository.ApplicationSubmissionRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private static final int MAX_TEXT = 5000;

    private final ApplicationFormRepository formRepository;
    private final ApplicationSubmissionRepository submissionRepository;
    private final MemberRepository memberRepository;
    private final FileService fileService;
    private final RegexService regexService;
    private final HtmlSanitizer htmlSanitizer;
    private final JsonMapper jsonMapper;

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
                .build()).getId();
    }

    @Transactional
    public void updateForm(final Long id, final FormSaveRequest request) {
        findForm(id).update(request.title().trim(), htmlSanitizer.sanitize(request.contentHtml()), writeFields(request.fields()),
                request.startDateTime(), request.endDateTime(), request.maximumSubmissions(), request.allowMultiple(),
                request.isEnabled());
    }

    @Transactional
    public void deleteForm(final Long id) {
        findForm(id).delete();
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
        findForm(formId);
        Page<ApplicationSubmission> page = submissionRepository.search(formId, status, pageable);
        Map<Long, String> usernames = memberRepository.findAllById(page.map(ApplicationSubmission::getMemberId).toList()).stream()
                .collect(Collectors.toMap(Member::getId, Member::getUsername));
        return page.map(submission -> SubmissionResponse.of(submission, readAnswers(submission),
                usernames.getOrDefault(submission.getMemberId(), "(탈퇴 회원)"), true));
    }

    @Transactional
    public SubmissionResponse review(final Long submissionId, final ReviewRequest request) {
        ApplicationSubmission submission = submissionRepository.findById(submissionId).orElseThrow(BusinessException::notFound);
        submission.review(request.status(), blankToNull(request.resultMessage()), blankToNull(request.adminMemo()));
        return SubmissionResponse.of(submission, readAnswers(submission), null, true);
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
        return FormResponse.of(form, readFields(form), submissionRepository.countActive(form.getId(), SubmissionStatus.WITHDRAWN),
                form.isOpenAt(LocalDateTime.now()), applied);
    }

    private ApplicationForm findForm(final Long id) {
        return formRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private static String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

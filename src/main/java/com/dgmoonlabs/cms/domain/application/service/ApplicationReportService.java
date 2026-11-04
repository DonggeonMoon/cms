package com.dgmoonlabs.cms.domain.application.service;

import com.dgmoonlabs.cms.domain.application.constant.FormFieldType;
import com.dgmoonlabs.cms.domain.application.constant.SubmissionStatus;
import com.dgmoonlabs.cms.domain.application.dto.ApplicationDtos.FieldSummary;
import com.dgmoonlabs.cms.domain.application.dto.ApplicationDtos.OptionCount;
import com.dgmoonlabs.cms.domain.application.dto.ApplicationDtos.SummaryResponse;
import com.dgmoonlabs.cms.domain.application.entity.ApplicationForm;
import com.dgmoonlabs.cms.domain.application.entity.ApplicationSubmission;
import com.dgmoonlabs.cms.domain.application.entity.FormField;
import com.dgmoonlabs.cms.domain.application.repository.ApplicationFormRepository;
import com.dgmoonlabs.cms.domain.application.repository.ApplicationSubmissionRepository;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationReportService {
    private static final DateTimeFormatter CREATED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Map<SubmissionStatus, String> STATUS_LABELS = Map.of(
            SubmissionStatus.RECEIVED, "접수", SubmissionStatus.REVIEWING, "검토 중", SubmissionStatus.ACCEPTED, "선정",
            SubmissionStatus.REJECTED, "미선정", SubmissionStatus.WITHDRAWN, "취소");

    private final ApplicationFormRepository formRepository;
    private final ApplicationSubmissionRepository submissionRepository;
    private final MemberRepository memberRepository;
    private final JsonMapper jsonMapper;

    @Transactional(readOnly = true)
    public SummaryResponse summarize(final Long formId, final SubmissionStatus status) {
        ApplicationForm form = findForm(formId);
        List<Map<String, Object>> answers = submissions(formId, status).stream().map(this::readAnswers).toList();
        List<FieldSummary> fields = readFields(form).stream()
                .filter(field -> field.type() != FormFieldType.FILE)
                .map(field -> summarize(field, answers))
                .toList();
        return new SummaryResponse(answers.size(), fields);
    }

    @Transactional(readOnly = true)
    public String exportCsv(final Long formId, final SubmissionStatus status) {
        ApplicationForm form = findForm(formId);
        List<FormField> fields = readFields(form);
        List<ApplicationSubmission> submissions = submissions(formId, status);
        Map<Long, Member> members = memberRepository.findAllById(submissions.stream().map(ApplicationSubmission::getMemberId).toList())
                .stream().collect(Collectors.toMap(Member::getId, Function.identity()));

        StringBuilder csv = new StringBuilder("﻿");
        List<String> header = new ArrayList<>(List.of("신청 번호", "아이디", "이름", "상태", "신청 일시"));
        fields.forEach(field -> header.add(field.label()));
        header.add("결과 안내");
        header.add("관리자 메모");
        appendRow(csv, header);
        for (ApplicationSubmission submission : submissions) {
            Member member = members.get(submission.getMemberId());
            Map<String, Object> answers = readAnswers(submission);
            List<String> row = new ArrayList<>(List.of(String.valueOf(submission.getId()),
                    member == null ? "(탈퇴 회원)" : member.getUsername(), member == null ? "" : member.getName(),
                    STATUS_LABELS.get(submission.getStatus()), submission.getCreatedAt().format(CREATED_AT)));
            fields.forEach(field -> row.add(format(field, answers.get(field.key()))));
            row.add(Objects.toString(submission.getResultMessage(), ""));
            row.add(Objects.toString(submission.getAdminMemo(), ""));
            appendRow(csv, row);
        }
        return csv.toString();
    }

    private static FieldSummary summarize(final FormField field, final List<Map<String, Object>> answers) {
        List<Object> values = answers.stream().map(answer -> answer.get(field.key())).filter(Objects::nonNull).toList();
        long answered = values.size();
        List<OptionCount> options = switch (field.type()) {
            case SELECT, RADIO -> count(field.optionsOrEmpty(), values.stream().map(String::valueOf).toList(), answered);
            case CHECKBOX -> field.optionsOrEmpty().isEmpty()
                    ? count(List.of("예"), values.stream().map(value -> "예").toList(), answers.size())
                    : count(field.optionsOrEmpty(), values.stream().flatMap(value -> ((Collection<?>) value).stream())
                    .map(String::valueOf).toList(), answered);
            default -> List.of();
        };
        if (field.type() != FormFieldType.NUMBER || values.isEmpty()) {
            return new FieldSummary(field.key(), field.label(), field.type(), answered, options, null, null, null);
        }
        List<BigDecimal> numbers = values.stream().map(value -> new BigDecimal(String.valueOf(value))).toList();
        BigDecimal sum = numbers.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return new FieldSummary(field.key(), field.label(), field.type(), answered, options,
                sum.divide(BigDecimal.valueOf(numbers.size()), 2, RoundingMode.HALF_UP),
                Collections.min(numbers), Collections.max(numbers));
    }

    // 복수 선택은 응답자 수를 분모로 써서 비율 합이 100%를 넘을 수 있다.
    private static List<OptionCount> count(final List<String> options, final List<String> picked, final long denominator) {
        Map<String, Long> counts = picked.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return options.stream().map(option -> {
            long count = counts.getOrDefault(option, 0L);
            return new OptionCount(option, count, denominator == 0 ? 0 : Math.round(count * 1000.0 / denominator) / 10.0);
        }).toList();
    }

    private static String format(final FormField field, final Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Collection<?> values) {
            return values.stream().map(String::valueOf).collect(Collectors.joining(", "));
        }
        if (field.type() == FormFieldType.FILE) {
            return "/api/files/" + value + "/download";
        }
        return Boolean.TRUE.equals(value) ? "예" : String.valueOf(value);
    }

    private static void appendRow(final StringBuilder csv, final List<String> cells) {
        csv.append(cells.stream().map(ApplicationReportService::escape).collect(Collectors.joining(","))).append("\r\n");
    }

    // 스프레드시트가 =, +, -, @ 로 시작하는 칸을 수식으로 실행하지 않도록 앞에 '를 붙인다.
    private static String escape(final String cell) {
        String value = !cell.isEmpty() && "=+-@\t\r".indexOf(cell.charAt(0)) >= 0 ? "'" + cell : cell;
        return value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
                ? "\"" + value.replace("\"", "\"\"") + "\"" : value;
    }

    private List<ApplicationSubmission> submissions(final Long formId, final SubmissionStatus status) {
        return submissionRepository.findAllForExport(formId, status, SubmissionStatus.WITHDRAWN);
    }

    private ApplicationForm findForm(final Long id) {
        return formRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private List<FormField> readFields(final ApplicationForm form) {
        return jsonMapper.readValue(form.getFields(), new TypeReference<List<FormField>>() {
        });
    }

    private Map<String, Object> readAnswers(final ApplicationSubmission submission) {
        return jsonMapper.readValue(submission.getAnswers(), new TypeReference<LinkedHashMap<String, Object>>() {
        });
    }
}

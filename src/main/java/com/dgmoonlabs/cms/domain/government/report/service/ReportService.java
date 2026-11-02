package com.dgmoonlabs.cms.domain.government.report.service;

import com.dgmoonlabs.cms.domain.board.constant.CommentStatus;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.government.report.constant.ReportStatus;
import com.dgmoonlabs.cms.domain.government.report.dto.ReportDtos.*;
import com.dgmoonlabs.cms.domain.government.report.entity.Report;
import com.dgmoonlabs.cms.domain.government.report.entity.answer.ReportAnswer;
import com.dgmoonlabs.cms.domain.government.report.repository.ReportAnswerRepository;
import com.dgmoonlabs.cms.domain.government.report.repository.ReportRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.html.HtmlSanitizer;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportAnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;
    private final HtmlSanitizer htmlSanitizer;

    @Transactional
    public Long createReport(final ReportCreateRequest request) {
        Optional<LoginUser> member = CurrentUser.get().filter(user -> user.userType() == UserType.MEMBER);
        if (member.isEmpty() && (isBlank(request.reporterName()) || isBlank(request.password()))) {
            throw new BusinessException(ErrorCode.GUEST_PASSWORD_REQUIRED);
        }
        String html = htmlSanitizer.sanitize(request.contentHtml());
        return reportRepository.save(Report.builder()
                .title(request.title())
                .contentHtml(html)
                .contentText(htmlSanitizer.toPlainText(html))
                .userId(member.map(LoginUser::id).orElse(null))
                .reporterName(member.map(LoginUser::name).orElse(request.reporterName()))
                .password(member.isPresent() ? null : passwordEncoder.encode(request.password()))
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .status(ReportStatus.RECEIVED)
                .build()).getId();
    }

    @Transactional(readOnly = true)
    public Page<ReportSummary> getMyReports(final Long memberId, final Pageable pageable) {
        return reportRepository.findAllByUserIdAndDeletedAtIsNullOrderByIdDesc(memberId, pageable).map(ReportSummary::from);
    }

    // reports hold personal data, so only the reporter (member, or guest with the password) and admins can open one
    @Transactional(readOnly = true)
    public ReportDetail getReport(final Long id, final String password) {
        Report report = findReport(id);
        boolean owner = report.getUserId() != null && CurrentUser.is(UserType.MEMBER, report.getUserId());
        boolean guestMatches = report.getUserId() == null && password != null && report.getPassword() != null
                && passwordEncoder.matches(password, report.getPassword());
        if (!CurrentUser.isAdmin() && !owner && !guestMatches) {
            throw new BusinessException(ErrorCode.ARTICLE_PASSWORD_MISMATCH);
        }
        return ReportDetail.of(report, answerRepository.findAllByReportIdAndDeletedAtIsNullOrderByIdAsc(id).stream()
                .map(AnswerResponse::from).toList());
    }

    @Transactional(readOnly = true)
    public Page<ReportSummary> searchReports(final ReportStatus status, final String keyword, final Pageable pageable) {
        return reportRepository.search(status, keyword == null ? "" : keyword.trim(), pageable).map(ReportSummary::from);
    }

    @Transactional
    public void changeStatus(final Long id, final ReportStatus status) {
        findReport(id).changeStatus(status);
    }

    @Transactional
    public Long answer(final Long reportId, final ReportAnswerRequest request) {
        Report report = findReport(reportId);
        LoginUser admin = CurrentUser.get().orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        String html = htmlSanitizer.sanitize(request.contentHtml());
        Long id = answerRepository.save(ReportAnswer.builder()
                .reportId(reportId)
                .order(answerRepository.countByReportIdAndDeletedAtIsNull(reportId))
                .title(request.title())
                .contentHtml(html)
                .contentText(htmlSanitizer.toPlainText(html))
                .authorName(admin.name())
                .status(CommentStatus.APPROVED)
                .build()).getId();
        report.changeStatus(ReportStatus.ANSWERED);
        return id;
    }

    @Transactional
    public void deleteAnswer(final Long reportId, final Long answerId) {
        answerRepository.findByIdAndReportIdAndDeletedAtIsNull(answerId, reportId)
                .orElseThrow(BusinessException::notFound)
                .delete();
    }

    @Transactional
    public void deleteReport(final Long id) {
        findReport(id).delete();
    }

    private Report findReport(final Long id) {
        return reportRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}

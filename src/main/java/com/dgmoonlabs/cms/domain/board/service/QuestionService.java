package com.dgmoonlabs.cms.domain.board.service;

import com.dgmoonlabs.cms.domain.board.dto.AnswerSaveRequest;
import com.dgmoonlabs.cms.domain.board.dto.QuestionResponse;
import com.dgmoonlabs.cms.domain.board.dto.QuestionSaveRequest;
import com.dgmoonlabs.cms.domain.board.entity.qna.Answer;
import com.dgmoonlabs.cms.domain.board.entity.qna.Question;
import com.dgmoonlabs.cms.domain.board.repository.AnswerRepository;
import com.dgmoonlabs.cms.domain.board.repository.QuestionRepository;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.html.HtmlSanitizer;
import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import com.dgmoonlabs.cms.domain.common.guestposting.GuestPostingPolicy;
import com.dgmoonlabs.cms.domain.common.guestposting.GuestPostingService;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final PasswordEncoder passwordEncoder;
    private final HtmlSanitizer htmlSanitizer;
    private final GuestPostingService guestPostingService;

    @Transactional(readOnly = true)
    public Page<QuestionResponse.Summary> getQuestions(final String keyword, final boolean unansweredOnly,
                                                      final Pageable pageable) {
        Page<Question> questions = questionRepository.search(keyword == null ? "" : keyword.trim(), unansweredOnly, pageable);
        Set<Long> answered = answerRepository.findAllByQuestionIdInAndDeletedAtIsNull(
                        questions.getContent().stream().map(Question::getId).toList())
                .stream().map(Answer::getQuestionId).collect(Collectors.toSet());
        return questions.map(question -> QuestionResponse.Summary.of(question, answered.contains(question.getId())));
    }

    @Transactional
    public QuestionResponse.Detail getQuestion(final Long id, final String password) {
        Question question = findQuestion(id);
        boolean ownerOrAdmin = isOwnerOrAdmin(question);
        boolean guestMatches = guestPasswordMatches(question, password);
        if (question.isSecret() && !ownerOrAdmin && !guestMatches) {
            throw new BusinessException(ErrorCode.ARTICLE_PASSWORD_MISMATCH);
        }
        question.increaseHit();
        Answer answer = answerRepository.findByQuestionIdAndDeletedAtIsNull(id).orElse(null);
        return QuestionResponse.Detail.of(question, answer, ownerOrAdmin || question.isWrittenByGuest(),
                ownerOrAdmin || guestMatches);
    }

    @Transactional
    public Long createQuestion(final QuestionSaveRequest request, final String ipAddress) {
        Optional<LoginUser> user = CurrentUser.get();
        if (user.isEmpty() && (isBlank(request.authorName()) || isBlank(request.password()))) {
            throw new BusinessException(ErrorCode.GUEST_PASSWORD_REQUIRED);
        }
        if (user.isEmpty()) {
            guestPolicy.requireContacts(request.phoneNumber(), request.email());
        }
        String html = htmlSanitizer.sanitize(request.contentHtml());
        Question question = Question.builder()
                .title(request.title())
                .contentHtml(html)
                .contentText(htmlSanitizer.toPlainText(html))
                .email(user.isEmpty() ? guestPolicy.keepEmail(request.email()) : request.email())
                .phoneNumber(user.isEmpty() ? guestPolicy.keepPhone(request.phoneNumber()) : request.phoneNumber())
                .isSecret(request.isSecret())
                .userType(user.map(LoginUser::userType).orElse(UserType.GUEST))
                .userId(user.map(LoginUser::id).orElse(null))
                .authorName(user.map(LoginUser::name).orElse(request.authorName()))
                .password(user.isPresent() ? null : passwordEncoder.encode(request.password()))
                .ipAddress(ipAddress)
                .build();
        return questionRepository.save(question).getId();
    }

    @Transactional
    public void updateQuestion(final Long id, final QuestionSaveRequest request) {
        Question question = findQuestion(id);
        requireModifiable(question, request.password());
        if (answerRepository.findByQuestionIdAndDeletedAtIsNull(id).isPresent() && !CurrentUser.isAdmin()) {
            throw new BusinessException(ErrorCode.ALREADY_ANSWERED);
        }
        String html = htmlSanitizer.sanitize(request.contentHtml());
        question.update(request.title(), html, htmlSanitizer.toPlainText(html), request.email(), request.phoneNumber(),
                request.isSecret());
    }

    @Transactional
    public void deleteQuestion(final Long id, final String password) {
        Question question = findQuestion(id);
        requireModifiable(question, password);
        question.delete();
    }

    @Transactional
    public void saveAnswer(final Long questionId, final AnswerSaveRequest request) {
        findQuestion(questionId);
        LoginUser admin = CurrentUser.get().orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        String html = htmlSanitizer.sanitize(request.contentHtml());
        String text = htmlSanitizer.toPlainText(html);

        Optional<Answer> existing = answerRepository.findByQuestionId(questionId);
        if (existing.isPresent()) {
            existing.get().restore();
            existing.get().update(html, text, admin.id(), admin.name());
            return;
        }
        answerRepository.save(Answer.builder()
                .questionId(questionId)
                .contentHtml(html)
                .contentText(text)
                .userType(UserType.ADMIN)
                .userId(admin.id())
                .authorName(admin.name())
                .build());
    }

    @Transactional
    public void deleteAnswer(final Long questionId) {
        answerRepository.findByQuestionIdAndDeletedAtIsNull(questionId)
                .orElseThrow(BusinessException::notFound)
                .delete();
    }

    private void requireModifiable(final Question question, final String password) {
        if (isOwnerOrAdmin(question)) {
            return;
        }
        if (!guestPasswordMatches(question, password)) {
            throw new BusinessException(question.isWrittenByGuest() ? ErrorCode.ARTICLE_PASSWORD_MISMATCH : ErrorCode.ACCESS_DENIED);
        }
    }

    private boolean isOwnerOrAdmin(final Question question) {
        return CurrentUser.isAdmin() || question.getUserId() != null && CurrentUser.is(question.getUserType(), question.getUserId());
    }

    private boolean guestPasswordMatches(final Question question, final String password) {
        return question.isWrittenByGuest() && password != null && question.getPassword() != null
                && passwordEncoder.matches(password, question.getPassword());
    }

    private Question findQuestion(final Long id) {
        return questionRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}

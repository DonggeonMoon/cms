package com.dgmoonlabs.cms.domain.board.dto;

import com.dgmoonlabs.cms.domain.board.entity.qna.Answer;
import com.dgmoonlabs.cms.domain.board.entity.qna.Question;

import java.time.LocalDateTime;

public final class QuestionResponse {
    private QuestionResponse() {
    }

    public record Summary(Long id, String title, String authorName, boolean isSecret, boolean answered, int hit,
                          LocalDateTime createdAt) {
        public static Summary of(final Question question, final boolean answered) {
            return new Summary(question.getId(), question.getTitle(), question.getAuthorName(), question.isSecret(),
                    answered, question.getHit(), question.getCreatedAt());
        }
    }

    public record Detail(Long id, String title, String contentHtml, String authorName, String email, boolean isSecret,
                         int hit, boolean canModify, AnswerDetail answer, LocalDateTime createdAt) {
        public static Detail of(final Question question, final Answer answer, final boolean canModify, final boolean showEmail) {
            return new Detail(question.getId(), question.getTitle(), question.getContentHtml(), question.getAuthorName(),
                    showEmail ? question.getEmail() : null, question.isSecret(), question.getHit(), canModify,
                    answer == null ? null : AnswerDetail.from(answer), question.getCreatedAt());
        }
    }

    public record AnswerDetail(String contentHtml, String authorName, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        public static AnswerDetail from(final Answer answer) {
            return new AnswerDetail(answer.getContentHtml(), answer.getAuthorName(), answer.getCreatedAt(),
                    answer.getModifiedAt());
        }
    }
}

package com.dgmoonlabs.cms.domain.course.dto;

import com.dgmoonlabs.cms.domain.course.constant.RevealPolicy;
import com.dgmoonlabs.cms.domain.course.constant.ScorePolicy;
import com.dgmoonlabs.cms.domain.course.entity.Quiz;
import com.dgmoonlabs.cms.domain.course.entity.QuizQuestion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public final class QuizDtos {
    private QuizDtos() {
    }

    public record QuizSaveRequest(
            @NotBlank @Size(max = 200) String title,
            @NotEmpty @Size(max = 30) List<@Valid QuizQuestion> questions,
            @Min(1) @Max(100) Integer passScore,
            Boolean isUsing,
            @Positive Integer retakeLimit,
            @Positive Integer timeLimitMinutes,
            boolean shuffleQuestions,
            boolean shuffleOptions,
            RevealPolicy revealPolicy,
            ScorePolicy scorePolicy
    ) {
        public int passScoreOrDefault() {
            return passScore == null ? Quiz.DEFAULT_PASS_SCORE : passScore;
        }

        public boolean isUsingOrDefault() {
            return isUsing == null || isUsing;
        }

        public RevealPolicy revealPolicyOrDefault() {
            return revealPolicy == null ? RevealPolicy.IMMEDIATELY : revealPolicy;
        }

        public ScorePolicy scorePolicyOrDefault() {
            return scorePolicy == null ? ScorePolicy.HIGHEST : scorePolicy;
        }
    }

    public record QuizResponse(Long id, String title, List<QuizQuestion> questions, int passScore, boolean isUsing,
                               Integer retakeLimit, Integer timeLimitMinutes, boolean shuffleQuestions, boolean shuffleOptions,
                               RevealPolicy revealPolicy, ScorePolicy scorePolicy, int itemCount, LocalDateTime createdAt) {
        public static QuizResponse of(final Quiz quiz, final int itemCount) {
            return new QuizResponse(quiz.getId(), quiz.getTitle(), quiz.getQuestions(), quiz.getPassScore(), quiz.isUsing(),
                    quiz.getRetakeLimit(), quiz.getTimeLimitMinutes(), quiz.isShuffleQuestions(), quiz.isShuffleOptions(),
                    quiz.getRevealPolicy(), quiz.getScorePolicy(), itemCount, quiz.getCreatedAt());
        }
    }

    public record QuizQuestionStat(String question, long attemptCount, long wrongCount, double wrongRate) {
    }

    public record QuizStatisticsResponse(Long quizId, String title, long totalAttempts, long uniqueTestTakers,
                                         double averageScore, double passRate, List<QuizQuestionStat> questions) {
    }
}

package com.dgmoonlabs.cms.domain.course.service;

import com.dgmoonlabs.cms.domain.course.dto.QuizDtos.*;
import com.dgmoonlabs.cms.domain.course.entity.CourseLesson;
import com.dgmoonlabs.cms.domain.course.entity.Quiz;
import com.dgmoonlabs.cms.domain.course.entity.QuizAttempt;
import com.dgmoonlabs.cms.domain.course.entity.QuizQuestion;
import com.dgmoonlabs.cms.domain.course.repository.CourseLessonRepository;
import com.dgmoonlabs.cms.domain.course.repository.QuizAttemptRepository;
import com.dgmoonlabs.cms.domain.course.repository.QuizRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.site.SiteContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final CourseLessonRepository lessonRepository;
    private final QuizAttemptRepository attemptRepository;

    @Transactional(readOnly = true)
    public Page<QuizResponse> getQuizzes(final String keyword, final Pageable pageable) {
        Page<Quiz> quizzes = quizRepository.findAllBySiteIdAndDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdDesc(
                SiteContextHolder.currentSiteId(), Objects.requireNonNullElse(keyword, ""), pageable);
        Map<Long, Long> counts = lessonRepository.findAllByQuizIdInAndDeletedAtIsNull(quizzes.map(Quiz::getId).toList()).stream()
                .collect(Collectors.groupingBy(CourseLesson::getQuizId, Collectors.counting()));
        return quizzes.map(quiz -> QuizResponse.of(quiz, counts.getOrDefault(quiz.getId(), 0L).intValue()));
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuiz(final Long id) {
        Quiz quiz = findQuiz(id);
        return QuizResponse.of(quiz, lessonRepository.existsByQuizIdAndDeletedAtIsNull(id) ? 1 : 0);
    }

    /** 퀴즈를 쓰는 모든 항목의 응시 기록을 모아 평균 점수·통과율과, 문항별 오답률을 계산한다. */
    @Transactional(readOnly = true)
    public QuizStatisticsResponse getStatistics(final Long id) {
        Quiz quiz = findQuiz(id);
        List<Long> lessonIds = lessonRepository.findAllByQuizIdInAndDeletedAtIsNull(List.of(id)).stream()
                .map(CourseLesson::getId).toList();
        List<QuizAttempt> attempts = lessonIds.isEmpty() ? List.of() : attemptRepository.findAllByLessonIdIn(lessonIds);
        long total = attempts.size();
        long uniqueTestTakers = attempts.stream().map(QuizAttempt::getMemberId).distinct().count();
        double averageScore = total == 0 ? 0 : attempts.stream().mapToInt(QuizAttempt::getScore).average().orElse(0);
        long passedCount = attempts.stream().filter(QuizAttempt::isPassed).count();
        double passRate = total == 0 ? 0 : passedCount * 100.0 / total;

        List<QuizQuestion> questions = quiz.getQuestions();
        List<QuizQuestionStat> questionStats = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            int index = i;
            List<QuizAttempt> answered = attempts.stream().filter(attempt -> attempt.getAnswers().size() > index).toList();
            long wrong = answered.stream().filter(attempt -> !attempt.getAnswers().get(index).equals(questions.get(index).answerIndex())).count();
            double wrongRate = answered.isEmpty() ? 0 : wrong * 100.0 / answered.size();
            questionStats.add(new QuizQuestionStat(questions.get(index).question(), answered.size(), wrong, wrongRate));
        }
        return new QuizStatisticsResponse(quiz.getId(), quiz.getTitle(), total, uniqueTestTakers, averageScore, passRate, questionStats);
    }

    @Transactional
    public Long createQuiz(final QuizSaveRequest request) {
        validate(request);
        Quiz quiz = quizRepository.save(Quiz.builder()
                .siteId(SiteContextHolder.currentSiteId())
                .title(request.title().trim())
                .questions(request.questions())
                .passScore(request.passScoreOrDefault())
                .isUsing(request.isUsingOrDefault())
                .retakeLimit(request.retakeLimit())
                .timeLimitMinutes(request.timeLimitMinutes())
                .shuffleQuestions(request.shuffleQuestions())
                .shuffleOptions(request.shuffleOptions())
                .revealPolicy(request.revealPolicyOrDefault())
                .scorePolicy(request.scorePolicyOrDefault())
                .build());
        return quiz.getId();
    }

    @Transactional
    public void updateQuiz(final Long id, final QuizSaveRequest request) {
        validate(request);
        findQuiz(id).update(request.title().trim(), request.questions(), request.passScoreOrDefault(), request.isUsingOrDefault(),
                request.retakeLimit(), request.timeLimitMinutes(), request.shuffleQuestions(), request.shuffleOptions(),
                request.revealPolicyOrDefault(), request.scorePolicyOrDefault());
    }

    @Transactional
    public void deleteQuiz(final Long id) {
        Quiz quiz = findQuiz(id);
        if (lessonRepository.existsByQuizIdAndDeletedAtIsNull(id)) {
            throw new BusinessException(ErrorCode.QUIZ_IN_USE);
        }
        quiz.delete();
    }

    private static void validate(final QuizSaveRequest request) {
        if (request.questions().stream().anyMatch(question -> question.answerIndex() >= question.options().size())) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ);
        }
    }

    private Quiz findQuiz(final Long id) {
        return quizRepository.findByIdAndSiteIdAndDeletedAtIsNull(id, SiteContextHolder.currentSiteId()).orElseThrow(BusinessException::notFound);
    }
}

package com.dgmoonlabs.cms.domain.course.service;

import com.dgmoonlabs.cms.domain.course.entity.Quiz;
import com.dgmoonlabs.cms.domain.course.entity.QuizQuestion;
import com.dgmoonlabs.cms.domain.course.repository.QuizRepository;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * "스트리밍/교육" 개편 전의 교육과정 데이터를 새 구조로 옮긴다.
 * - 개편 전에 있던 과정은 전부 실제 교육과정이었으므로 isCourse=true로 맞춘다. 이 작업은 "is_course=false인 행을 전부 true로"가
 *   아니라 한 번만 실행해야 한다 — 그렇지 않으면 개편 뒤 관리자가 일부러 "교육과정으로 사용"을 꺼서 만든 일반 시리즈까지
 *   재시작마다 다시 true로 뒤집어 버린다. 그래서 별도 마커 테이블로 한 번만 실행했는지 기억한다.
 * - 차시에 인라인 JSON으로 들어있던 퀴즈(옛 quiz·pass_score 컬럼)를 독립 Quiz 엔티티로 꺼내 quiz_id로 연결한다.
 *   이 작업은 quiz_id가 비어 있는 행만 찾으므로 몇 번을 실행해도 안전하다(멱등).
 * ddl-auto=update 는 옛 컬럼을 지우지 않아 그대로 남지만, 이전 뒤로는 쓰지 않는다.
 */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class CourseQuizMigration implements ApplicationRunner {
    private static final String MARKER = "course_is_course_backfill_v1";

    private final JdbcTemplate jdbcTemplate;
    private final QuizRepository quizRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        markExistingCoursesAsCourse();
        migrateInlineQuizzes();
    }

    private void markExistingCoursesAsCourse() {
        if (!hasColumn("course", "is_course")) {
            return;
        }
        jdbcTemplate.execute("""
                create table if not exists schema_backfill (
                    name varchar(200) primary key,
                    applied_at timestamp not null default now()
                )
                """);
        Integer already = jdbcTemplate.queryForObject("select count(*) from schema_backfill where name = ?", Integer.class, MARKER);
        if (already != null && already > 0) {
            return;
        }
        int updated = jdbcTemplate.update("update course set is_course = true where is_course = false");
        jdbcTemplate.update("insert into schema_backfill(name) values (?) on conflict do nothing", MARKER);
        if (updated > 0) {
            log.info("기존 교육과정 {}건을 isCourse=true로 맞췄습니다.", updated);
        }
    }

    private void migrateInlineQuizzes() {
        if (!hasColumn("course_lesson", "quiz") || !hasColumn("course_lesson", "quiz_id")) {
            return;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                select cl.id as lesson_id, cl.title as lesson_title, cl.pass_score as pass_score,
                       cl.quiz::text as quiz_text, c.site_id as site_id
                from course_lesson cl
                join course c on c.id = cl.course_id
                where cl.quiz_id is null and cl.quiz is not null and cl.deleted_at is null
                """);
        int migrated = 0;
        for (Map<String, Object> row : rows) {
            try {
                List<QuizQuestion> questions = objectMapper.readValue((String) row.get("quiz_text"),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, QuizQuestion.class));
                if (questions.isEmpty()) {
                    continue;
                }
                Integer passScore = (Integer) row.get("pass_score");
                Quiz quiz = quizRepository.save(Quiz.builder()
                        .siteId((Long) row.get("site_id"))
                        .title((String) row.get("lesson_title"))
                        .questions(questions)
                        .passScore(passScore == null ? Quiz.DEFAULT_PASS_SCORE : passScore)
                        .isUsing(true)
                        .build());
                jdbcTemplate.update("update course_lesson set quiz_id = ? where id = ?", quiz.getId(), row.get("lesson_id"));
                migrated++;
            } catch (Exception e) {
                log.warn("차시 {}의 인라인 퀴즈를 옮기지 못했습니다: {}", row.get("lesson_id"), e.getMessage());
            }
        }
        if (migrated > 0) {
            log.info("차시에 있던 인라인 퀴즈 {}건을 독립 Quiz로 옮겼습니다.", migrated);
        }
    }

    private boolean hasColumn(final String table, final String column) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_name = ? and column_name = ?",
                Integer.class, table, column);
        return count != null && count > 0;
    }
}

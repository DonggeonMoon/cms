package com.dgmoonlabs.cms.domain.course;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.JsonNode;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseApiTest extends ApiTestSupport {
    private static final String QUIZ = """
            [{"question":"1+1은?","options":["1","2","3"],"answerIndex":1},
             {"question":"하늘 색은?","options":["파랑","빨강"],"answerIndex":0}]""";

    @Test
    void lessonsCompleteByWatchingAndPassingQuizzesInOrder() throws Exception {
        MockHttpSession admin = admin();
        long streamingId = createdId(postJson("/api/admin/streaming", admin, """
                {"title":"1강 영상","fileId":%d,"durationSeconds":100,"completionRate":90}
                """.formatted(upload(admin))));

        postJson("/api/admin/quizzes", admin, """
                {"title":"정답 오류","questions":[{"question":"?","options":["a","b"],"answerIndex":5}]}
                """).andExpect(jsonPath("$.code").value("INVALID_QUIZ"));
        long quizId = createdId(postJson("/api/admin/quizzes", admin, """
                {"title":"2차시 확인 문제","questions":%s,"passScore":100}
                """.formatted(QUIZ)).andExpect(status().isCreated()));

        postJson("/api/admin/courses", admin, """
                {"title":"빈 차시","isUsing":true,"isCourse":true,"lessons":[{"title":"아무것도 없음"}]}
                """).andExpect(jsonPath("$.code").value("INVALID_LESSON"));
        postJson("/api/admin/courses", admin, """
                {"title":"교육과정 아님","isUsing":true,"isCourse":false,"lessons":[{"title":"퀴즈","quizId":%d}]}
                """.formatted(quizId)).andExpect(jsonPath("$.code").value("QUIZ_REQUIRES_COURSE_MODE"));
        long courseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"기초 과정","sequential":true,"isUsing":true,"isCourse":true,"lessons":[
                  {"title":"1차시 강의","streamingId":%d},
                  {"title":"2차시 확인 문제","quizId":%d}]}
                """.formatted(streamingId, quizId)).andExpect(status().isCreated()));

        getJson("/api/admin/courses/" + courseId, admin).andExpect(jsonPath("$.lessons[1].quiz[0].answerIndex").value(1));
        JsonNode course = body(getJson("/api/courses/" + courseId, null)
                .andExpect(jsonPath("$.lessonCount").value(2))
                .andExpect(jsonPath("$.lessons[0].streamingTitle").value("1강 영상"))
                .andExpect(jsonPath("$.lessons[1].quiz[0].answerIndex").doesNotExist())
                .andExpect(jsonPath("$.lessons[1].passScore").value(100)));
        long quizLesson = course.get("lessons").get(1).get("id").asLong();
        String quizUrl = "/api/courses/%d/lessons/%d/quiz".formatted(courseId, quizLesson);

        MockHttpSession learner = member("student1");
        postJson(quizUrl, learner, "{\"answers\":[1,0]}").andExpect(jsonPath("$.code").value("COURSE_NOT_ENROLLED"));
        postJson("/api/courses/%d/enroll".formatted(courseId), learner, "")
                .andExpect(jsonPath("$.myProgress.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.lessons[1].locked").value(true));
        postJson(quizUrl, learner, "{\"answers\":[1,0]}").andExpect(jsonPath("$.code").value("LESSON_LOCKED"));

        postJson("/api/streaming/" + streamingId + "/start", learner, "{}").andExpect(status().isOk());
        postJson("/api/streaming/" + streamingId + "/progress", learner, """
                {"ranges":[{"start":0,"end":95}],"position":95}
                """).andExpect(jsonPath("$.status").value("COMPLETED"));
        getJson("/api/courses/" + courseId, learner)
                .andExpect(jsonPath("$.lessons[0].completed").value(true))
                .andExpect(jsonPath("$.lessons[1].locked").value(false))
                .andExpect(jsonPath("$.myProgress.completedLessons").value(1));

        postJson(quizUrl + "/start", learner, "").andExpect(jsonPath("$.questions.length()").value(2));
        postJson(quizUrl, learner, "{\"answers\":[1]}").andExpect(status().isBadRequest());
        postJson(quizUrl, learner, "{\"answers\":[1,1]}")
                .andExpect(jsonPath("$.score").value(50))
                .andExpect(jsonPath("$.passed").value(false))
                .andExpect(jsonPath("$.correct[1]").value(false))
                .andExpect(jsonPath("$.myProgress.status").value("IN_PROGRESS"));
        postJson(quizUrl + "/start", learner, "");
        postJson(quizUrl, learner, "{\"answers\":[1,0]}")
                .andExpect(jsonPath("$.passed").value(true))
                .andExpect(jsonPath("$.attempts").value(2))
                .andExpect(jsonPath("$.myProgress.status").value("COMPLETED"))
                .andExpect(jsonPath("$.myProgress.completedAt").exists());

        getJson("/api/courses/me", learner)
                .andExpect(jsonPath("$.content[0].title").value("기초 과정"))
                .andExpect(jsonPath("$.content[0].myProgress.completedLessons").value(2));
        JsonNode learners = body(getJson("/api/admin/courses/%d/learners".formatted(courseId), admin)
                .andExpect(jsonPath("$.content[0].username").value("student1"))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.content[0].totalLessons").value(2)));
        long learnerId = learners.get("content").get(0).get("memberId").asLong();
        getJson("/api/admin/courses/%d/learners/%d".formatted(courseId, learnerId), admin)
                .andExpect(jsonPath("$.lessons[0].progressRate").value(95))
                .andExpect(jsonPath("$.lessons[0].lastPositionSeconds").value(95))
                .andExpect(jsonPath("$.lessons[0].completed").value(true))
                .andExpect(jsonPath("$.lessons[1].quizScore").value(100))
                .andExpect(jsonPath("$.lessons[1].completed").value(true))
                .andExpect(jsonPath("$.myProgress.completedLessons").value(2));

        putJson("/api/admin/courses/" + courseId, admin, """
                {"title":"기초 과정","sequential":true,"isUsing":true,"isCourse":true,"lessons":[
                  {"id":%d,"title":"확인 문제를 앞으로","quizId":%d},
                  {"title":"1차시 강의","streamingId":%d}]}
                """.formatted(quizLesson, quizId, streamingId)).andExpect(status().isNoContent());
        getJson("/api/courses/" + courseId, learner)
                .andExpect(jsonPath("$.lessons[0].title").value("확인 문제를 앞으로"))
                .andExpect(jsonPath("$.lessons[0].quizPassed").value(true))
                .andExpect(jsonPath("$.lessons[1].completed").value(true));
    }

    @Test
    void enrollmentApplicationFlowCoversFreeApplyAndApprovalModes() throws Exception {
        MockHttpSession admin = admin();
        long streamingId = createdId(postJson("/api/admin/streaming", admin, """
                {"title":"강의","fileId":%d,"durationSeconds":100,"completionRate":90}
                """.formatted(upload(admin))));
        MockHttpSession learner = member("student2");

        long approvalCourseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"승인제 과정","isUsing":true,"isCourse":true,"enrollmentMode":"APPROVAL","lessons":[{"title":"1강","streamingId":%d}]}
                """.formatted(streamingId)).andExpect(status().isCreated()));

        postJson("/api/courses/%d/enroll".formatted(approvalCourseId), learner, "")
                .andExpect(jsonPath("$.myApplicationStatus").value("APPLIED"))
                .andExpect(jsonPath("$.myProgress").value(nullValue()));

        JsonNode applications = body(getJson("/api/admin/courses/%d/applications".formatted(approvalCourseId), admin)
                .andExpect(jsonPath("$.content[0].status").value("APPLIED"))
                .andExpect(jsonPath("$.content[0].username").value("student2")));
        long applicationId = applications.get("content").get(0).get("id").asLong();

        postJson("/api/admin/courses/%d/applications/%d/reject".formatted(approvalCourseId, applicationId), admin, "")
                .andExpect(status().isNoContent());
        postJson("/api/admin/courses/%d/applications/%d/reject".formatted(approvalCourseId, applicationId), admin, "")
                .andExpect(jsonPath("$.code").value("COURSE_APPLICATION_ALREADY_DECIDED"));
        getJson("/api/courses/" + approvalCourseId, learner).andExpect(jsonPath("$.myApplicationStatus").value("REJECTED"));

        // 반려 후 재신청하면 새 신청 건이 쌓이고, 승인하면 바로 수강 가능해진다.
        postJson("/api/courses/%d/enroll".formatted(approvalCourseId), learner, "")
                .andExpect(jsonPath("$.myApplicationStatus").value("APPLIED"));
        JsonNode reapplied = body(getJson("/api/admin/courses/%d/applications".formatted(approvalCourseId), admin));
        long secondApplicationId = reapplied.get("content").get(0).get("id").asLong();
        postJson("/api/admin/courses/%d/applications/%d/approve".formatted(approvalCourseId, secondApplicationId), admin, "")
                .andExpect(status().isNoContent());
        getJson("/api/courses/" + approvalCourseId, learner)
                .andExpect(jsonPath("$.myApplicationStatus").value("APPROVED"))
                .andExpect(jsonPath("$.myProgress.status").value("IN_PROGRESS"));

        // 승인 전에는 신청을 취소할 수 있다.
        long cancelCourseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"승인제 과정2","isUsing":true,"isCourse":true,"enrollmentMode":"APPROVAL","lessons":[{"title":"1강","streamingId":%d}]}
                """.formatted(streamingId)).andExpect(status().isCreated()));
        postJson("/api/courses/%d/enroll".formatted(cancelCourseId), learner, "")
                .andExpect(jsonPath("$.myApplicationStatus").value("APPLIED"));
        postJson("/api/courses/%d/applications/cancel".formatted(cancelCourseId), learner, "").andExpect(status().isNoContent());
        getJson("/api/courses/" + cancelCourseId, learner).andExpect(jsonPath("$.myApplicationStatus").value("CANCELED"));
        postJson("/api/courses/%d/applications/cancel".formatted(cancelCourseId), learner, "").andExpect(status().isNotFound());

        // 신청 후 수강: 신청하면 신청 기록은 남지만 바로 수강할 수 있다.
        long applyCourseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"신청후수강 과정","isUsing":true,"isCourse":true,"enrollmentMode":"APPLY","lessons":[{"title":"1강","streamingId":%d}]}
                """.formatted(streamingId)).andExpect(status().isCreated()));
        postJson("/api/courses/%d/enroll".formatted(applyCourseId), learner, "")
                .andExpect(jsonPath("$.myApplicationStatus").value("APPROVED"))
                .andExpect(jsonPath("$.myProgress.status").value("IN_PROGRESS"));

        // 자유 수강(기본값): 신청 기록 없이 바로 수강할 수 있다.
        long freeCourseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"자유 과정","isUsing":true,"isCourse":true,"lessons":[{"title":"1강","streamingId":%d}]}
                """.formatted(streamingId)).andExpect(status().isCreated()));
        postJson("/api/courses/%d/enroll".formatted(freeCourseId), learner, "")
                .andExpect(jsonPath("$.myApplicationStatus").value(nullValue()))
                .andExpect(jsonPath("$.myProgress.status").value("IN_PROGRESS"));
    }

    @Test
    void accessPeriodBlocksContentAndAdminCanExtendIt() throws Exception {
        MockHttpSession admin = admin();
        long quizId = createdId(postJson("/api/admin/quizzes", admin, """
                {"title":"기간 테스트 퀴즈","questions":%s,"passScore":50}
                """.formatted(QUIZ)).andExpect(status().isCreated()));
        MockHttpSession learner = member("student3");

        java.time.LocalDateTime past = java.time.LocalDateTime.now().minusDays(10);
        java.time.LocalDateTime recentPast = java.time.LocalDateTime.now().minusDays(1);
        java.time.LocalDateTime future = java.time.LocalDateTime.now().plusDays(30);

        // 신청 기간이 이미 끝난 과정은 신청(수강) 자체가 막힌다.
        long closedApplicationCourseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"신청 마감 과정","isUsing":true,"isCourse":true,
                 "applicationStartAt":"%s","applicationEndAt":"%s",
                 "lessons":[{"title":"1강","quizId":%d}]}
                """.formatted(past, recentPast, quizId)).andExpect(status().isCreated()));
        postJson("/api/courses/%d/enroll".formatted(closedApplicationCourseId), learner, "")
                .andExpect(jsonPath("$.code").value("COURSE_APPLICATION_PERIOD_CLOSED"));

        // 고정 기간인데 이미 끝난 과정: 수강 신청은 자유 수강이라 되지만, 콘텐츠 접근(퀴즈 제출)은 막힌다.
        long endedCourseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"기간 종료 과정","isUsing":true,"isCourse":true,
                 "accessPeriodMode":"FIXED","accessStartAt":"%s","accessEndAt":"%s",
                 "lessons":[{"title":"1강","quizId":%d}]}
                """.formatted(past, recentPast, quizId)).andExpect(status().isCreated()));
        JsonNode course = body(postJson("/api/courses/%d/enroll".formatted(endedCourseId), learner, "")
                .andExpect(jsonPath("$.myAccessOpen").value(false)));
        long lessonId = course.get("lessons").get(0).get("id").asLong();
        String quizUrl = "/api/courses/%d/lessons/%d/quiz".formatted(endedCourseId, lessonId);
        postJson(quizUrl, learner, "{\"answers\":[1,0]}").andExpect(jsonPath("$.code").value("COURSE_ACCESS_PERIOD_ENDED"));

        // 관리자가 개별 수강자의 기간을 연장하면 다시 접근할 수 있다.
        JsonNode learners = body(getJson("/api/admin/courses/%d/learners".formatted(endedCourseId), admin));
        long learnerId = learners.get("content").get(0).get("memberId").asLong();
        postJson("/api/admin/courses/%d/learners/%d/extend".formatted(endedCourseId, learnerId), admin, """
                {"accessUntil":"%s"}
                """.formatted(future)).andExpect(status().isNoContent());
        postJson(quizUrl + "/start", learner, "");
        postJson(quizUrl, learner, "{\"answers\":[1,0]}").andExpect(jsonPath("$.passed").value(true));
        getJson("/api/courses/" + endedCourseId, learner).andExpect(jsonPath("$.myAccessOpen").value(true));

        // 고정 기간 설정에 시작·종료 중 하나라도 없으면 거부된다.
        postJson("/api/admin/courses", admin, """
                {"title":"설정 오류","isUsing":true,"isCourse":true,"accessPeriodMode":"FIXED","lessons":[{"title":"1강","quizId":%d}]}
                """.formatted(quizId)).andExpect(jsonPath("$.code").value("COURSE_ACCESS_PERIOD_REQUIRED"));
    }

    @Test
    void materialsAreDownloadableOnlyByEnrolledLearnersWithinAccessPeriod() throws Exception {
        MockHttpSession admin = admin();
        long streamingId = createdId(postJson("/api/admin/streaming", admin, """
                {"title":"강의","fileId":%d,"durationSeconds":100,"completionRate":90}
                """.formatted(upload(admin))));
        long courseMaterialFileId = upload(admin);
        long lessonMaterialFileId = upload(admin);

        long courseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"자료 포함 과정","isUsing":true,"isCourse":true,
                 "materialFileIds":[%d],
                 "lessons":[{"title":"1강","streamingId":%d,"materialFileIds":[%d]}]}
                """.formatted(courseMaterialFileId, streamingId, lessonMaterialFileId)).andExpect(status().isCreated()));

        JsonNode course = body(getJson("/api/courses/" + courseId, null)
                .andExpect(jsonPath("$.materials[0].id").exists())
                .andExpect(jsonPath("$.lessons[0].materials[0].id").exists()));
        long courseMaterialId = course.get("materials").get(0).get("id").asLong();
        long lessonMaterialId = course.get("lessons").get(0).get("materials").get(0).get("id").asLong();
        String downloadUrl = "/api/courses/%d/materials/%d/download".formatted(courseId, courseMaterialId);

        MockHttpSession learner = member("student4");
        getJson(downloadUrl, learner).andExpect(jsonPath("$.code").value("COURSE_NOT_ENROLLED"));

        postJson("/api/courses/%d/enroll".formatted(courseId), learner, "").andExpect(status().isOk());
        getJson(downloadUrl, learner).andExpect(status().isOk());
        getJson("/api/courses/%d/materials/%d/download".formatted(courseId, lessonMaterialId), learner).andExpect(status().isOk());

        // 교육과정 자료로 쓴 파일은 보호 파일로 바뀌어 일반 다운로드 경로로는 받을 수 없다.
        getJson("/api/files/" + courseMaterialFileId + "/download", learner).andExpect(status().isNotFound());

        // 자료를 다시 저장할 때 비워두면 모두 지워진다.
        putJson("/api/admin/courses/" + courseId, admin, """
                {"title":"자료 포함 과정","isUsing":true,"isCourse":true,
                 "lessons":[{"id":%d,"title":"1강","streamingId":%d}]}
                """.formatted(course.get("lessons").get(0).get("id").asLong(), streamingId)).andExpect(status().isNoContent());
        getJson("/api/courses/" + courseId, null).andExpect(jsonPath("$.materials").isEmpty());
        getJson(downloadUrl, learner).andExpect(status().isNotFound());
    }

    @Test
    void mandatoryLessonsAndCompletionThresholdDecideAttendance() throws Exception {
        MockHttpSession admin = admin();
        long quizId = createdId(postJson("/api/admin/quizzes", admin, """
                {"title":"공용 퀴즈","questions":%s,"passScore":50}
                """.formatted(QUIZ)).andExpect(status().isCreated()));

        java.time.LocalDateTime past = java.time.LocalDateTime.now().minusDays(1);
        java.time.LocalDateTime future = java.time.LocalDateTime.now().plusDays(30);

        // 4개 항목 중 1번만 필수, 수료 기준은 전체 진도율 50%(2개) 이상. 출석 기준도 켠다.
        long courseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"필수 항목 과정","isUsing":true,"isCourse":true,
                 "accessPeriodMode":"FIXED","accessStartAt":"%s","accessEndAt":"%s",
                 "completionThresholdPercent":50,"attendanceRequired":true,
                 "lessons":[
                   {"title":"1강(필수)","quizId":%d,"mandatory":true},
                   {"title":"2강","quizId":%d},
                   {"title":"3강","quizId":%d},
                   {"title":"4강","quizId":%d}]}
                """.formatted(past, future, quizId, quizId, quizId, quizId)).andExpect(status().isCreated()));

        MockHttpSession learner = member("student5");
        JsonNode course = body(postJson("/api/courses/%d/enroll".formatted(courseId), learner, ""));
        long lesson1 = course.get("lessons").get(0).get("id").asLong();
        long lesson2 = course.get("lessons").get(1).get("id").asLong();
        long lesson3 = course.get("lessons").get(2).get("id").asLong();

        // 필수가 아닌 2·3강만 통과하면 진도율은 기준(50%)을 넘지만, 필수 항목(1강)을 안 해서 수료되지 않는다.
        postJson("/api/courses/%d/lessons/%d/quiz/start".formatted(courseId, lesson2), learner, "");
        postJson("/api/courses/%d/lessons/%d/quiz".formatted(courseId, lesson2), learner, "{\"answers\":[1,0]}");
        postJson("/api/courses/%d/lessons/%d/quiz/start".formatted(courseId, lesson3), learner, "");
        postJson("/api/courses/%d/lessons/%d/quiz".formatted(courseId, lesson3), learner, "{\"answers\":[1,0]}");
        getJson("/api/courses/" + courseId, learner)
                .andExpect(jsonPath("$.myProgress.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.myProgress.completedLessons").value(2))
                .andExpect(jsonPath("$.myAttendanceOk").value(false));

        // 필수 항목(1강)까지 마치면 전체 진도율(3/4=75%)도 기준을 넘어 수료되고, 기간 안에 끝냈으므로 출석도 인정된다.
        postJson("/api/courses/%d/lessons/%d/quiz/start".formatted(courseId, lesson1), learner, "");
        postJson("/api/courses/%d/lessons/%d/quiz".formatted(courseId, lesson1), learner, "{\"answers\":[1,0]}");
        getJson("/api/courses/" + courseId, learner)
                .andExpect(jsonPath("$.myProgress.status").value("COMPLETED"))
                .andExpect(jsonPath("$.myAttendanceOk").value(true));

        getJson("/api/admin/courses/%d/learners".formatted(courseId), admin)
                .andExpect(jsonPath("$.content[0].attendanceOk").value(true));
    }

    @Test
    void quizRetakeLimitShuffleGrantAndScorePolicyWork() throws Exception {
        MockHttpSession admin = admin();
        String quizWithExplanation = """
                [{"question":"1+1은?","options":["1","2","3"],"answerIndex":1,"explanation":"1 더하기 1은 2"},
                 {"question":"하늘 색은?","options":["파랑","빨강"],"answerIndex":0,"explanation":"하늘은 파랗다"}]""";
        long quizId = createdId(postJson("/api/admin/quizzes", admin, """
                {"title":"옵션 퀴즈","questions":%s,"passScore":50,"retakeLimit":1,
                 "shuffleQuestions":true,"shuffleOptions":true,"revealPolicy":"NEVER","scorePolicy":"HIGHEST"}
                """.formatted(quizWithExplanation)).andExpect(status().isCreated()));
        long courseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"퀴즈 옵션 과정","isUsing":true,"isCourse":true,"lessons":[{"title":"1강","quizId":%d}]}
                """.formatted(quizId)).andExpect(status().isCreated()));

        MockHttpSession learner = member("student6");
        JsonNode course = body(postJson("/api/courses/%d/enroll".formatted(courseId), learner, ""));
        long lessonId = course.get("lessons").get(0).get("id").asLong();
        String base = "/api/courses/%d/lessons/%d/quiz".formatted(courseId, lessonId);

        // 문제·보기가 섞여도 섞인 화면 기준으로 정답을 골라 내면(원래 순서를 몰라도) 정답으로 채점된다.
        JsonNode started = body(postJson(base + "/start", learner, "")
                .andExpect(jsonPath("$.attemptsUsed").value(0))
                .andExpect(jsonPath("$.attemptsAllowed").value(1)));
        List<Integer> correctAnswers = correctShuffledAnswers(started);
        postJson(base, learner, "{\"answers\":" + correctAnswers + "}")
                .andExpect(jsonPath("$.score").value(100))
                .andExpect(jsonPath("$.passed").value(true))
                .andExpect(jsonPath("$.revealed").value(false))
                .andExpect(jsonPath("$.answerIndexes").value(nullValue()))
                .andExpect(jsonPath("$.explanations").value(nullValue()))
                .andExpect(jsonPath("$.attemptsRemaining").value(0))
                .andExpect(jsonPath("$.durationSeconds").isNumber());

        // 재응시 허용 횟수(1회)를 다 썼으므로 다시 시작할 수 없다.
        postJson(base + "/start", learner, "").andExpect(jsonPath("$.code").value("QUIZ_RETAKE_LIMIT_REACHED"));

        // 관리자가 추가 응시 횟수를 부여하면 다시 시작할 수 있다.
        postJson("/api/admin/courses/%d/lessons/%d/learners/%d/grant-attempts".formatted(courseId, lessonId, memberIdOf(learner)),
                admin, "{\"count\":1}").andExpect(status().isNoContent());
        JsonNode restarted = body(postJson(base + "/start", learner, "")
                .andExpect(jsonPath("$.attemptsUsed").value(1))
                .andExpect(jsonPath("$.attemptsAllowed").value(2)));
        // 이번엔 일부러 틀린다 - 최고 점수(HIGHEST) 정책이라 이전 100점이 그대로 인정되어야 한다.
        List<Integer> wrongAnswers = restarted.get("questions").size() > 0
                ? correctShuffledAnswers(restarted).stream().map(a -> a == 0 ? 1 : 0).toList() : List.of();
        postJson(base, learner, "{\"answers\":" + wrongAnswers + "}")
                .andExpect(jsonPath("$.attemptsRemaining").value(0));

        getJson("/api/courses/" + courseId, learner).andExpect(jsonPath("$.lessons[0].quizScore").value(100));
    }

    private static List<Integer> correctShuffledAnswers(final JsonNode started) {
        List<Integer> answers = new java.util.ArrayList<>();
        for (JsonNode question : started.get("questions")) {
            String correctText = question.get("question").asText().equals("1+1은?") ? "2" : "파랑";
            List<String> options = new java.util.ArrayList<>();
            question.get("options").forEach(o -> options.add(o.asText()));
            answers.add(options.indexOf(correctText));
        }
        return answers;
    }

    private long memberIdOf(final MockHttpSession learner) throws Exception {
        return body(getJson("/api/members/me", learner)).get("id").asLong();
    }

    private long upload(final MockHttpSession session) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "lecture.mp4", "application/octet-stream", "0123456789".getBytes());
        return body(mockMvc.perform(multipart("/api/files").file(file).session(session).header("X-CMS-Zone", "admin").with(csrf()))
                .andExpect(status().isCreated())).get("id").asLong();
    }
}

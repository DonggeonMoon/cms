package com.dgmoonlabs.cms.domain.course;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseLessonCommentFlowTest extends ApiTestSupport {
    @Test
    void enrolledMemberCanAskAQuestionAndAdminCanReplyAndOthersCannotEditOrSeeBeyondOneDepth() throws Exception {
        MockHttpSession admin = admin();
        long quizId = createdId(postJson("/api/admin/quizzes", admin, """
                {"title":"퀴즈","questions":[{"question":"1+1은?","options":["1","2"],"answerIndex":1}],"passScore":50}
                """).andExpect(status().isCreated()));
        long courseId = createdId(postJson("/api/admin/courses", admin, """
                {"title":"무료 과정","isUsing":true,"isCourse":true,"enrollmentMode":"FREE",
                 "lessons":[{"title":"1강","quizId":%d}]}
                """.formatted(quizId)).andExpect(status().isCreated()));
        JsonNode course = body(getJson("/api/courses/" + courseId, null));
        long lessonId = course.get("lessons").get(0).get("id").asLong();

        MockHttpSession learner = member("learner1");
        postJson("/api/courses/%d/lessons/%d/comments".formatted(courseId, lessonId), learner, "{\"content\":\"질문\"}")
                .andExpect(jsonPath("$.code").value("COURSE_NOT_ENROLLED"));
        postJson("/api/courses/%d/enroll".formatted(courseId), learner, "");

        long questionId = createdId(postJson("/api/courses/%d/lessons/%d/comments".formatted(courseId, lessonId), learner,
                "{\"content\":\"이 부분을 모르겠어요\"}").andExpect(status().isCreated()));

        MockHttpSession other = member("learner2");
        postJson("/api/courses/%d/enroll".formatted(courseId), other, "");

        postJson("/api/courses/%d/lessons/%d/comments".formatted(courseId, lessonId), admin,
                "{\"parentId\":%d,\"content\":\"이렇게 하시면 됩니다\"}".formatted(questionId))
                .andExpect(status().isCreated());

        JsonNode comments = body(getJson("/api/courses/%d/lessons/%d/comments".formatted(courseId, lessonId), learner));
        org.junit.jupiter.api.Assertions.assertEquals(2, comments.size());
        org.junit.jupiter.api.Assertions.assertEquals(0, comments.get(0).get("depth").asInt());
        org.junit.jupiter.api.Assertions.assertTrue(comments.get(0).get("canEdit").asBoolean());
        org.junit.jupiter.api.Assertions.assertEquals(1, comments.get(1).get("depth").asInt());
        long replyId = comments.get(1).get("id").asLong();

        // 답글(깊이 1)에는 더 답글을 달 수 없다.
        postJson("/api/courses/%d/lessons/%d/comments".formatted(courseId, lessonId), learner,
                "{\"parentId\":%d,\"content\":\"더 깊은 답글\"}".formatted(replyId))
                .andExpect(jsonPath("$.code").value("FEATURE_NOT_SUPPORTED"));

        // 다른 수강생은 남의 질문을 수정/삭제할 수 없다.
        putJson("/api/courses/%d/lessons/%d/comments/%d".formatted(courseId, lessonId, questionId), other,
                "{\"content\":\"수정 시도\"}").andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        deleteJson("/api/courses/%d/lessons/%d/comments/%d".formatted(courseId, lessonId, questionId), other, null)
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        // 작성자 본인은 수정할 수 있고, 관리자는 아무 질문/답글이나 삭제할 수 있다.
        putJson("/api/courses/%d/lessons/%d/comments/%d".formatted(courseId, lessonId, questionId), learner,
                "{\"content\":\"수정했어요\"}").andExpect(status().isNoContent());
        deleteJson("/api/courses/%d/lessons/%d/comments/%d".formatted(courseId, lessonId, questionId), admin, null)
                .andExpect(status().isNoContent());

        JsonNode afterDelete = body(getJson("/api/courses/%d/lessons/%d/comments".formatted(courseId, lessonId), learner));
        org.junit.jupiter.api.Assertions.assertTrue(afterDelete.get(0).get("deleted").asBoolean());
    }
}

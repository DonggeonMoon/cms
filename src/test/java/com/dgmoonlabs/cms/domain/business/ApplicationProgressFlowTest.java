package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 다단계 심사(서류→면접→합격)는 응답 집계 외에, 신청·지원 대상으로 공통 진행 단계·메시지 기능을 그대로 쓴다. */
class ApplicationProgressFlowTest extends ApiTestSupport {
    @Test
    void hiringApplicationGoesThroughDocumentAndInterviewStages() throws Exception {
        MockHttpSession admin = admin();
        long formId = createdId(postJson("/api/admin/applications", admin, """
                {"title":"신입 개발자 채용","isEnabled":true,"fields":[
                  {"key":"resume","label":"자기소개","type":"TEXTAREA","required":true}]}
                """).andExpect(status().isCreated()));

        MockHttpSession member = member("jobseeker");
        long submissionId = body(postJson("/api/applications/%d/submissions".formatted(formId), member, """
                {"answers":{"resume":"3년차 백엔드 개발자입니다."}}
                """).andExpect(status().isCreated())).get("id").asLong();

        postJson("/api/admin/progress/APPLICATION/templates", admin, """
                {"code":"DOCUMENT","label":"서류 심사","sequence":1}
                """).andExpect(status().isOk());
        postJson("/api/admin/progress/APPLICATION/templates", admin, """
                {"code":"INTERVIEW","label":"면접","sequence":2}
                """).andExpect(status().isOk());

        postJson("/api/admin/progress/APPLICATION/" + submissionId + "/advance", admin, "{\"stageCode\":\"DOCUMENT\"}")
                .andExpect(jsonPath("$.stageLabel").value("서류 심사"));
        getJson("/api/progress?targetType=APPLICATION&targetId=" + submissionId, member)
                .andExpect(jsonPath("$.currentStageCode").value("DOCUMENT"));
        getJson("/api/progress?targetType=APPLICATION&targetId=" + submissionId, member("other"))
                .andExpect(status().isNotFound());

        postJson("/api/admin/messages?targetType=APPLICATION&targetId=" + submissionId, admin, """
                {"content":"서류 통과하셨습니다. 면접 일정을 안내드립니다."}
                """).andExpect(jsonPath("$.senderType").value("ADMIN"));
        postJson("/api/admin/progress/APPLICATION/" + submissionId + "/advance", admin, "{\"stageCode\":\"INTERVIEW\"}")
                .andExpect(jsonPath("$.stageLabel").value("면접"));

        getJson("/api/progress?targetType=APPLICATION&targetId=" + submissionId, member)
                .andExpect(jsonPath("$.history.length()").value(2))
                .andExpect(jsonPath("$.currentStageCode").value("INTERVIEW"));
        getJson("/api/messages?targetType=APPLICATION&targetId=" + submissionId, member)
                .andExpect(jsonPath("$.length()").value(1));

        putJson("/api/admin/applications/submissions/" + submissionId, admin, """
                {"status":"ACCEPTED","resultMessage":"최종 합격을 축하드립니다."}
                """).andExpect(jsonPath("$.status").value("ACCEPTED"));
    }
}

package com.dgmoonlabs.cms.domain.application;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicationApiTest extends ApiTestSupport {
    private static final String FORM = """
            {"title":"청년 창업 지원금","isEnabled":true,"maximumSubmissions":2,"fields":[
              {"key":"company","label":"회사명","type":"TEXT","required":true},
              {"key":"stage","label":"사업 단계","type":"SELECT","required":true,"options":["예비 창업","초기 창업"]},
              {"key":"email","label":"이메일","type":"EMAIL","required":false},
              {"key":"agree","label":"서약","type":"CHECKBOX","required":true}]}
            """;

    @Test
    void membersApplyWithTheDefinedFieldsAndAdminsReview() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/applications", admin, FORM.replace("\"options\":[\"예비 창업\",\"초기 창업\"]", "\"options\":[]"))
                .andExpect(jsonPath("$.code").value("INVALID_FORM_FIELDS"));
        long formId = createdId(postJson("/api/admin/applications", admin, FORM).andExpect(status().isCreated()));
        getJson("/api/applications/" + formId, null)
                .andExpect(jsonPath("$.fields.length()").value(4))
                .andExpect(jsonPath("$.isOpen").value(true));

        MockHttpSession member = member("founder");
        postJson("/api/applications/%d/submissions".formatted(formId), member, """
                {"answers":{"company":"","stage":"성장기","email":"bad"}}
                """)
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'company')].message").value("회사명을(를) 입력해 주세요."))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'stage')].message").value("사업 단계 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')].message").value("이메일 형식이 올바르지 않습니다."));
        long submissionId = body(postJson("/api/applications/%d/submissions".formatted(formId), member, """
                {"answers":{"company":"두근랩","stage":"초기 창업","agree":true,"unknown":"x"}}
                """).andExpect(status().isCreated())).get("id").asLong();
        postJson("/api/applications/%d/submissions".formatted(formId), member, """
                {"answers":{"company":"두근랩","stage":"초기 창업","agree":true}}
                """).andExpect(jsonPath("$.code").value("ALREADY_APPLIED"));
        getJson("/api/applications/" + formId, member).andExpect(jsonPath("$.applied").value(true));

        getJson("/api/admin/applications/%d/submissions".formatted(formId), admin)
                .andExpect(jsonPath("$.content[0].username").value("founder"))
                .andExpect(jsonPath("$.content[0].answers.company").value("두근랩"))
                .andExpect(jsonPath("$.content[0].answers.unknown").doesNotExist());
        putJson("/api/admin/applications/submissions/" + submissionId, admin, """
                {"status":"ACCEPTED","resultMessage":"선정을 축하합니다","adminMemo":"서류 우수"}
                """).andExpect(jsonPath("$.status").value("ACCEPTED"));
        getJson("/api/applications/me", member)
                .andExpect(jsonPath("$.content[0].resultMessage").value("선정을 축하합니다"))
                .andExpect(jsonPath("$.content[0].adminMemo").doesNotExist());
        postJson("/api/applications/submissions/%d/withdraw".formatted(submissionId), member, "")
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
    }

    @Test
    void submissionsStopAtTheLimit() throws Exception {
        MockHttpSession admin = admin();
        long formId = createdId(postJson("/api/admin/applications", admin, FORM.replace("\"maximumSubmissions\":2", "\"maximumSubmissions\":1")));
        String answers = "{\"answers\":{\"company\":\"A\",\"stage\":\"예비 창업\",\"agree\":true}}";
        postJson("/api/applications/%d/submissions".formatted(formId), member("first"), answers).andExpect(status().isCreated());
        postJson("/api/applications/%d/submissions".formatted(formId), member("second"), answers)
                .andExpect(jsonPath("$.code").value("APPLICATION_FULL"));
    }
}

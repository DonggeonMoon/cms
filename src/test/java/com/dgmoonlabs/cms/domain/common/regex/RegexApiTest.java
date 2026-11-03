package com.dgmoonlabs.cms.domain.common.regex;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegexApiTest extends ApiTestSupport {
    private static final String JOIN = """
            {"username":"%s","password":"%s","passwordCheck":"%s","isPrivacyConsentAgreed":true,"name":"정규식"}
            """;
    private static final String JOIN_SETTINGS = """
            {"nameFormat":"FULL","collectedItems":["EMAIL","MOBILE_PHONE"],"regexes":{%s}}
            """;

    @Test
    void defaultRegexesAreRegisteredSelectedAndChecked() throws Exception {
        MockHttpSession admin = verified(admin());
        getJson("/api/admin/regexes", admin)
                .andExpect(jsonPath("$[?(@.name == '비밀번호')].usedBy[0]").value("비밀번호"))
                .andExpect(jsonPath("$[?(@.name == '결제번호')].pattern").value("^P\\d{14}-\\d{4}$"));
        getJson("/api/admin/members/join-settings", admin)
                .andExpect(jsonPath("$.regexes.PASSWORD").value(idOf(admin, "비밀번호")));

        postJson("/api/members", null, JOIN.formatted("regexuser", "onlyletters", "onlyletters"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("password"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("비밀번호는 영문과 숫자를 포함해 8~64자로 입력해 주세요."));
        getJson("/api/members/username-availability?username=AB", null)
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void joinSettingsChooseWhichRegexChecksAField() throws Exception {
        MockHttpSession admin = verified(admin());
        long digitsOnly = createdId(postJson("/api/admin/regexes", admin, """
                {"name":"숫자 비밀번호","pattern":"^\\\\d{6}$","message":"비밀번호는 숫자 {6}자리"}
                """).andExpect(status().isCreated()));
        putJson("/api/admin/members/join-settings", admin, JOIN_SETTINGS.formatted("\"PASSWORD\":" + digitsOnly))
                .andExpect(jsonPath("$.regexes.PASSWORD").value(digitsOnly));
        postJson("/api/members", null, JOIN.formatted("digits1", "abc12345", "abc12345"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("비밀번호는 숫자 {6}자리"));
        postJson("/api/members", null, JOIN.formatted("digits2", "123456", "123456")).andExpect(status().isCreated());

        putJson("/api/admin/members/join-settings", admin, JOIN_SETTINGS.formatted("\"PASSWORD\":null"))
                .andExpect(jsonPath("$.regexes.PASSWORD").doesNotExist());
        postJson("/api/members", null, JOIN.formatted("anything", "x", "x")).andExpect(status().isCreated());
        getJson("/api/admin/regexes", admin).andExpect(jsonPath("$[?(@.id == " + digitsOnly + ")].usedBy.length()").value(0));
    }

    @Test
    void numberRegexesAreChosenPerMenuAndMustFitGeneratedNumbers() throws Exception {
        MockHttpSession admin = verified(admin());
        long loose = createdId(postJson("/api/admin/regexes", admin, """
                {"name":"영문 대문자 시작","pattern":"^[A-Z].+$","message":"x"}
                """));
        long wrong = createdId(postJson("/api/admin/regexes", admin, """
                {"name":"ORD 형식","pattern":"^ORD-\\\\d+$","message":"x"}
                """));
        putJson("/api/admin/regex-bindings/ORDER_NUMBER", admin, "{\"regexRuleId\":" + wrong + "}")
                .andExpect(jsonPath("$.code").value("REGEX_NUMBER_FORMAT_MISMATCH"));
        putJson("/api/admin/regex-bindings/ORDER_NUMBER", admin, "{\"regexRuleId\":" + loose + "}")
                .andExpect(status().isNoContent());
        getJson("/api/admin/regex-bindings/ORDER_NUMBER", admin).andExpect(jsonPath("$.regexRuleId").value(loose));
        putJson("/api/admin/regexes/" + loose, admin, """
                {"name":"영문 대문자 시작","pattern":"^ORD-\\\\d+$","message":"x"}
                """).andExpect(jsonPath("$.code").value("REGEX_NUMBER_FORMAT_MISMATCH"));
        putJson("/api/admin/regex-bindings/PASSWORD", admin, "{\"regexRuleId\":null}")
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        postJson("/api/admin/regexes", admin, """
                {"name":"잘못","pattern":"^[a-","message":"x"}
                """).andExpect(jsonPath("$.fieldErrors[0].field").value("pattern"));
        postJson("/api/admin/regexes/test", admin, """
                {"pattern":"^\\\\d{3}-\\\\d{2}-\\\\d{5}$","value":"123-45-67890"}
                """).andExpect(jsonPath("$.matches").value(true));
    }

    private MockHttpSession verified(final MockHttpSession admin) throws Exception {
        postJson("/api/admin/reauthenticate", admin, "{\"password\":\"adminpass1\"}").andExpect(status().isNoContent());
        return admin;
    }

    private long idOf(final MockHttpSession admin, final String name) throws Exception {
        for (JsonNode rule : body(getJson("/api/admin/regexes", admin))) {
            if (rule.get("name").asText().equals(name)) {
                return rule.get("id").asLong();
            }
        }
        throw new AssertionError(name);
    }
}

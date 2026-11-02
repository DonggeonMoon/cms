package com.dgmoonlabs.cms.domain.government;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GovernmentApiTest extends ApiTestSupport {
    @Test
    void guestReportIsAnsweredAndOnlyVisibleWithPassword() throws Exception {
        long reportId = createdId(postJson("/api/reports", null, """
                {"title":"가로등 고장","contentHtml":"<p>3번가</p>","reporterName":"시민","phoneNumber":"010-1111-2222",
                 "password":"report1"}
                """).andExpect(status().isCreated()));

        getJson("/api/reports/" + reportId, null).andExpect(status().isForbidden());

        MockHttpSession admin = admin();
        getJson("/api/admin/reports?status=RECEIVED", admin).andExpect(jsonPath("$.content[0].id").value(reportId));
        postJson("/api/admin/reports/" + reportId + "/answers", admin, """
                {"title":"처리 완료","contentHtml":"<p>교체했습니다</p>"}
                """).andExpect(status().isCreated());

        postJson("/api/reports/" + reportId + "/access", null, "{\"password\":\"report1\"}")
                .andExpect(jsonPath("$.status").value("ANSWERED"))
                .andExpect(jsonPath("$.answers[0].title").value("처리 완료"))
                .andExpect(jsonPath("$.phoneNumber").value("010-1111-2222"));
    }

    @Test
    void membersRateEachMenuOnce() throws Exception {
        MockHttpSession admin = admin();
        long menuId = createdId(postJson("/api/admin/menus", admin, """
                {"name":"민원 안내","type":"CONTENT","targetId":1}
                """).andExpect(status().isCreated()));

        postJson("/api/satisfactions", null, "{\"menuId\":%d,\"score\":5}".formatted(menuId))
                .andExpect(status().isUnauthorized());

        MockHttpSession first = member("rater1");
        MockHttpSession second = member("rater2");
        postJson("/api/satisfactions", first, "{\"menuId\":%d,\"score\":5,\"opinion\":\"친절\"}".formatted(menuId))
                .andExpect(jsonPath("$.total").value(1));
        postJson("/api/satisfactions", first, "{\"menuId\":%d,\"score\":1}".formatted(menuId))
                .andExpect(jsonPath("$.code").value("ALREADY_SUBMITTED"));
        postJson("/api/satisfactions", second, "{\"menuId\":%d,\"score\":2}".formatted(menuId))
                .andExpect(jsonPath("$.average").value(3.5));

        getJson("/api/satisfactions?menuId=" + menuId, first).andExpect(jsonPath("$.myScore").value(5));
        getJson("/api/admin/satisfactions/" + menuId + "/opinions", admin)
                .andExpect(jsonPath("$.content[0].opinion").value("친절"));
    }
}

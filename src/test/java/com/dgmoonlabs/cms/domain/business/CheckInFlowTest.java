package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CheckInFlowTest extends ApiTestSupport {
    private static final String APPLICANT = "\"applicantName\":\"수강생\",\"phoneNumber\":\"010-1234-5678\"";

    @Test
    void confirmedReservationCanBeCheckedInOnceByCode() throws Exception {
        MockHttpSession admin = admin();
        long yoga = createdId(postJson("/api/admin/programs", admin, """
                {"name":"요가","price":0,"reservationType":"PROGRAM","isEnabled":true}
                """));

        MockHttpSession member = member("yogi");
        String code = body(postJson("/api/reservations", member, """
                {"programId":%d,"quantity":1,%s}
                """.formatted(yoga, APPLICANT)).andExpect(jsonPath("$.status").value("CONFIRMED"))).get("checkInCode").asText();

        postJson("/api/admin/reservations/check-in", admin, "{\"code\":\"NOPE\"}").andExpect(status().isNotFound());
        postJson("/api/admin/reservations/check-in", admin, "{\"code\":\"%s\"}".formatted(code))
                .andExpect(jsonPath("$.checkedInAt").exists());
        postJson("/api/admin/reservations/check-in", admin, "{\"code\":\"%s\"}".formatted(code))
                .andExpect(jsonPath("$.code").value("ALREADY_CHECKED_IN"));
    }
}

package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class ReceptionQueueFlowTest extends ApiTestSupport {
    private static final String APPLICANT = "\"applicantName\":\"환자\",\"phoneNumber\":\"010-1234-5678\"";
    private static final LocalDate DAY = LocalDate.now().plusDays(3);

    @Test
    void checkInAssignsQueueNumberAndAdminCanAdvanceIt() throws Exception {
        MockHttpSession admin = admin();
        long clinic = createdId(postJson("/api/admin/reservation-items", admin, """
                {"name":"진료","price":0,"reservationType":"CONSULTATION","slotMinutes":30,"maximumPerson":5,
                 "openTime":"10:00","closeTime":"11:00","startDateTime":"%sT00:00:00","endDateTime":"%sT23:59:00",
                 "isEnabled":true,"supportsQueue":true}
                """.formatted(DAY.minusDays(1), DAY.plusDays(1))));

        String code1 = body(postJson("/api/reservations", member("patient1"), """
                {"programId":%d,"quantity":1,"startAt":"%sT10:00:00",%s}
                """.formatted(clinic, DAY, APPLICANT)).andExpect(jsonPath("$.status").value("CONFIRMED")))
                .get("checkInCode").asText();
        String code2 = body(postJson("/api/reservations", member("patient2"), """
                {"programId":%d,"quantity":1,"startAt":"%sT10:00:00",%s}
                """.formatted(clinic, DAY, APPLICANT))).get("checkInCode").asText();

        long id1 = body(postJson("/api/admin/reservations/check-in", admin, "{\"code\":\"%s\"}".formatted(code1))
                .andExpect(jsonPath("$.queueNumber").value(1))
                .andExpect(jsonPath("$.queueStatus").value("WAITING"))).get("id").asLong();

        getJson("/api/admin/reservations/queue?programId=" + clinic, admin)
                .andExpect(jsonPath("$.length()").value(1));

        postJson("/api/admin/reservations/check-in", admin, "{\"code\":\"%s\"}".formatted(code2))
                .andExpect(jsonPath("$.queueNumber").value(2))
                .andExpect(jsonPath("$.queueStatus").value("WAITING"));

        getJson("/api/admin/reservations/queue?programId=" + clinic, admin)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].queueNumber").value(1))
                .andExpect(jsonPath("$[1].queueNumber").value(2));

        postJson("/api/admin/reservations/%d/queue-status".formatted(id1), admin, "{\"status\":\"CALLED\"}")
                .andExpect(jsonPath("$.queueStatus").value("CALLED"));
        postJson("/api/admin/reservations/%d/queue-status".formatted(id1), admin, "{\"status\":\"DONE\"}")
                .andExpect(jsonPath("$.queueStatus").value("DONE"));
    }

    @Test
    void checkInWithoutQueueSupportLeavesQueueNumberEmpty() throws Exception {
        MockHttpSession admin = admin();
        long yoga = createdId(postJson("/api/admin/reservation-items", admin, """
                {"name":"요가","price":0,"reservationType":"PROGRAM","isEnabled":true}
                """));
        String code = body(postJson("/api/reservations", member("yogi"), """
                {"programId":%d,"quantity":1,%s}
                """.formatted(yoga, APPLICANT))).get("checkInCode").asText();

        long id = body(postJson("/api/admin/reservations/check-in", admin, "{\"code\":\"%s\"}".formatted(code))
                .andExpect(jsonPath("$.queueNumber").doesNotExist())).get("id").asLong();

        postJson("/api/admin/reservations/%d/queue-status".formatted(id), admin, "{\"status\":\"CALLED\"}")
                .andExpect(jsonPath("$.code").value("QUEUE_NOT_APPLICABLE"));
    }
}

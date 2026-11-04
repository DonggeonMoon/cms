package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VisitServiceFlowTest extends ApiTestSupport {
    private static final String APPLICANT = "\"applicantName\":\"고객\",\"phoneNumber\":\"010-1234-5678\"";
    private static final LocalDate DAY = LocalDate.now().plusDays(3);

    @Test
    void visitsAreBookedPerSlotWithAddressAndClosedWithAWorkReport() throws Exception {
        MockHttpSession admin = admin();
        long programId = createdId(postJson("/api/admin/programs", admin, """
                {"name":"에어컨 청소","price":80000,"reservationType":"VISIT","slotMinutes":120,"maximumPerson":1,
                 "openTime":"09:00","closeTime":"13:00","startDateTime":"%sT00:00:00","endDateTime":"%sT23:59:00","isEnabled":true}
                """.formatted(DAY.minusDays(5), DAY.plusDays(5))).andExpect(status().isCreated()));
        getJson("/api/programs/%d/availability?date=%s".formatted(programId, DAY), null).andExpect(jsonPath("$.slots.length()").value(2));

        MockHttpSession customer = member("homeowner");
        String booking = "{\"programId\":%d,\"quantity\":1,\"startAt\":\"%sT09:00:00\",%s%s}";
        postJson("/api/reservations", customer, booking.formatted(programId, DAY, APPLICANT, ""))
                .andExpect(jsonPath("$.code").value("VISIT_ADDRESS_REQUIRED"));
        long reservationId = body(postJson("/api/reservations", customer, booking.formatted(programId, DAY, APPLICANT,
                ",\"visitPostalCode\":\"06000\",\"visitAddress\":\"서울 강남구 테헤란로 1\",\"visitDetailAddress\":\"101호\",\"requestMemo\":\"주차 가능\""))
                .andExpect(jsonPath("$.endAt").value(DAY + "T11:00:00"))
                .andExpect(jsonPath("$.visitAddress").value("서울 강남구 테헤란로 1"))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))).get("id").asLong();
        postJson("/api/reservations", member("neighbor"), booking.formatted(programId, DAY, APPLICANT, ",\"visitAddress\":\"서울\""))
                .andExpect(jsonPath("$.code").value("RESERVATION_SLOT_FULL"));

        putJson("/api/admin/reservations/%d/worker".formatted(reservationId), admin, "{\"workerName\":\"김기사\"}")
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
        postJson("/api/payments", customer, """
                {"targetType":"RESERVATION","targetId":%d,"amount":80000,"method":"CARD"}
                """.formatted(reservationId)).andExpect(status().is2xxSuccessful());
        postJson("/api/admin/reservations/%d/complete".formatted(reservationId), admin, "{}")
                .andExpect(jsonPath("$.code").value("WORKER_NOT_ASSIGNED"));
        putJson("/api/admin/reservations/%d/worker".formatted(reservationId), admin, """
                {"workerName":"김기사","workerPhone":"010-9999-0000"}
                """).andExpect(jsonPath("$.workerName").value("김기사"));
        getJson("/api/reservations/me", customer)
                .andExpect(jsonPath("$.content[0].workerPhone").value("010-9999-0000"))
                .andExpect(jsonPath("$.content[0].requestMemo").value("주차 가능"));

        postJson("/api/admin/reservations/%d/complete".formatted(reservationId), admin, "{\"report\":\"필터 교체\",\"photoFileIds\":[999999]}")
                .andExpect(status().isNotFound());
        postJson("/api/admin/reservations/%d/complete".formatted(reservationId), admin, "{\"report\":\"필터 교체 완료\"}")
                .andExpect(jsonPath("$.workCompletedAt").exists())
                .andExpect(jsonPath("$.workReport").value("필터 교체 완료"));
        postJson("/api/admin/reservations/%d/complete".formatted(reservationId), admin, "{}")
                .andExpect(jsonPath("$.code").value("WORK_ALREADY_COMPLETED"));
        postJson("/api/reservations/%d/cancel".formatted(reservationId), customer, "")
                .andExpect(jsonPath("$.code").value("WORK_ALREADY_COMPLETED"));
    }
}

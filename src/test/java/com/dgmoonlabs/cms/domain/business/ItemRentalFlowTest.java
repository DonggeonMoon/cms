package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemRentalFlowTest extends ApiTestSupport {
    private static final String APPLICANT = "\"applicantName\":\"대여자\",\"phoneNumber\":\"010-1234-5678\"";
    private static final LocalDate DAY = LocalDate.now().plusDays(2);

    @Test
    void itemsAreRentedWithinStockAndDepositIsRefundedOnReturn() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/programs", admin, "{\"name\":\"텐트\",\"price\":10000,\"reservationType\":\"ITEM_RENTAL\",\"isEnabled\":true}")
                .andExpect(jsonPath("$.code").value("RESERVATION_SETTINGS_REQUIRED"));
        long tent = createdId(postJson("/api/admin/programs", admin, """
                {"name":"텐트","price":10000,"reservationType":"ITEM_RENTAL","maximumPerson":3,"depositAmount":50000,
                 "maxRentalDays":5,"isEnabled":true}
                """).andExpect(status().isCreated()));
        getJson("/api/programs/" + tent, null).andExpect(jsonPath("$.depositAmount").value(50000));

        String rent = "{\"programId\":%d,\"quantity\":%d,\"startAt\":\"%sT00:00:00\",\"endAt\":\"%sT00:00:00\",%s}";
        MockHttpSession camper = member("camper");
        postJson("/api/reservations", camper, rent.formatted(tent, 1, DAY, DAY.plusDays(6), APPLICANT))
                .andExpect(jsonPath("$.code").value("INVALID_RESERVATION_TIME"));
        long reservationId = body(postJson("/api/reservations", camper, rent.formatted(tent, 2, DAY, DAY.plusDays(3), APPLICANT))
                .andExpect(jsonPath("$.totalPrice").value(2 * 3 * 10000 + 2 * 50000))
                .andExpect(jsonPath("$.depositAmount").value(100000))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))).get("id").asLong();

        getJson("/api/programs/%d/availability?from=%sT00:00:00&to=%sT00:00:00".formatted(tent, DAY.plusDays(2), DAY.plusDays(4)), null)
                .andExpect(jsonPath("$.remainingItems").value(1));
        MockHttpSession other = member("other");
        postJson("/api/reservations", other, rent.formatted(tent, 2, DAY.plusDays(2), DAY.plusDays(4), APPLICANT))
                .andExpect(jsonPath("$.code").value("RENTAL_OUT_OF_STOCK"));
        postJson("/api/reservations", other, rent.formatted(tent, 3, DAY.plusDays(3), DAY.plusDays(4), APPLICANT))
                .andExpect(status().isCreated());

        postJson("/api/admin/reservations/%d/return".formatted(reservationId), admin, "{}")
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
        postJson("/api/payments", camper, """
                {"targetType":"RESERVATION","targetId":%d,"amount":160000,"method":"CARD"}
                """.formatted(reservationId)).andExpect(jsonPath("$.status").value("APPROVED"));
        postJson("/api/admin/reservations/%d/return".formatted(reservationId), admin, "{\"deduction\":200000}")
                .andExpect(jsonPath("$.code").value("INVALID_DEPOSIT_DEDUCTION"));
        postJson("/api/admin/reservations/%d/return".formatted(reservationId), admin, """
                {"deduction":20000,"memo":"폴대 파손"}
                """)
                .andExpect(jsonPath("$.depositRefunded").value(80000))
                .andExpect(jsonPath("$.returnMemo").value("폴대 파손"))
                .andExpect(jsonPath("$.overdue").value(false))
                .andExpect(jsonPath("$.lateDays").value(0));
        getJson("/api/admin/payments?keyword=", admin).andExpect(jsonPath("$.content[0].refundedAmount").value(80000));
        postJson("/api/admin/reservations/%d/return".formatted(reservationId), admin, "{}")
                .andExpect(jsonPath("$.code").value("ALREADY_RETURNED"));
        postJson("/api/reservations/%d/cancel".formatted(reservationId), camper, "").andExpect(jsonPath("$.code").value("ALREADY_RETURNED"));

        getJson("/api/programs/%d/availability?from=%sT00:00:00&to=%sT00:00:00".formatted(tent, DAY.plusDays(1), DAY.plusDays(2)), null)
                .andExpect(jsonPath("$.remainingItems").value(3));
    }
}

package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RentalExtensionFlowTest extends ApiTestSupport {
    private static final String APPLICANT = "\"applicantName\":\"대여자\",\"phoneNumber\":\"010-1234-5678\"";
    private static final LocalDate DAY = LocalDate.now().plusDays(2);
    private static final String RENT = "{\"reservationItemId\":%d,\"quantity\":%d,\"startAt\":\"%sT00:00:00\",\"endAt\":\"%sT00:00:00\",%s}";

    @Autowired
    private EntityManager entityManager;

    @Test
    void extensionsHoldStockAndUnpaidFeesAndLateFeesComeOutOfTheDeposit() throws Exception {
        MockHttpSession admin = admin();
        long tent = createdId(postJson("/api/admin/reservation-items", admin, """
                {"name":"텐트","price":10000,"reservationType":"ITEM_RENTAL","maximumPerson":2,"depositAmount":50000,
                 "maxRentalDays":5,"lateFeePerDay":5000,"isEnabled":true}
                """).andExpect(status().isCreated()));
        getJson("/api/reservation-items/" + tent, null).andExpect(jsonPath("$.lateFeePerDay").value(5000));

        MockHttpSession camper = member("extender");
        long id = body(postJson("/api/reservations", camper, RENT.formatted(tent, 1, DAY, DAY.plusDays(2), APPLICANT))
                .andExpect(jsonPath("$.lateFeePerDay").value(5000))).get("id").asLong();
        String extend = "{\"endAt\":\"%sT00:00:00\"}";
        postJson("/api/reservations/%d/extend".formatted(id), camper, extend.formatted(DAY.plusDays(4)))
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
        postJson("/api/payments", camper, """
                {"targetType":"RESERVATION","targetId":%d,"amount":70000,"method":"CARD"}
                """.formatted(id)).andExpect(status().isOk());

        postJson("/api/reservations/%d/extend".formatted(id), camper, extend.formatted(DAY.plusDays(6)))
                .andExpect(jsonPath("$.code").value("INVALID_EXTENSION"));
        long extensionId = body(postJson("/api/reservations/%d/extend".formatted(id), camper, extend.formatted(DAY.plusDays(4)))
                .andExpect(jsonPath("$.endAt").value(DAY.plusDays(4) + "T00:00:00"))
                .andExpect(jsonPath("$.pendingExtensionFee").value(20000))).get("pendingExtensionId").asLong();
        postJson("/api/reservations/%d/extend".formatted(id), camper, extend.formatted(DAY.plusDays(5)))
                .andExpect(jsonPath("$.code").value("EXTENSION_PAYMENT_PENDING"));
        postJson("/api/payments", camper, """
                {"targetType":"RESERVATION_EXTENSION","targetId":%d,"amount":20000,"method":"CARD"}
                """.formatted(extensionId)).andExpect(jsonPath("$.status").value("APPROVED"));
        getJson("/api/reservations/me", camper).andExpect(jsonPath("$.content[0].pendingExtensionId").doesNotExist());

        MockHttpSession other = member("blocker");
        long blocking = body(postJson("/api/reservations", other, RENT.formatted(tent, 2, DAY.plusDays(4), DAY.plusDays(5), APPLICANT))
                .andExpect(status().isCreated())).get("id").asLong();
        postJson("/api/reservations/%d/extend".formatted(id), camper, extend.formatted(DAY.plusDays(5)))
                .andExpect(jsonPath("$.code").value("RENTAL_OUT_OF_STOCK"));
        postJson("/api/reservations/%d/cancel".formatted(blocking), other, "").andExpect(status().isNoContent());
        postJson("/api/reservations/%d/extend".formatted(id), camper, extend.formatted(DAY.plusDays(5)))
                .andExpect(jsonPath("$.pendingExtensionFee").value(10000));
        getJson("/api/admin/reservations/%d/extensions".formatted(id), admin)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("PAID"))
                .andExpect(jsonPath("$[1].status").value("PENDING_PAYMENT"));

        // 반납일이 하루 지난 상태로 만든다.
        entityManager.flush();
        entityManager.createQuery("update Reservation r set r.startAt = :start, r.endAt = :end where r.id = :id")
                .setParameter("start", LocalDate.now().minusDays(5).atStartOfDay())
                .setParameter("end", LocalDate.now().minusDays(1).atStartOfDay())
                .setParameter("id", id).executeUpdate();
        entityManager.clear();
        getJson("/api/reservations/me", camper)
                .andExpect(jsonPath("$.content[0].overdue").value(true))
                .andExpect(jsonPath("$.content[0].expectedLateFee").value(5000));

        postJson("/api/admin/reservations/%d/return".formatted(id), admin, "{\"deduction\":45000,\"memo\":\"폴대 파손\"}")
                .andExpect(jsonPath("$.lateFee").value(5000))
                .andExpect(jsonPath("$.unpaidExtensionFee").value(10000))
                .andExpect(jsonPath("$.depositRefunded").value(0))
                .andExpect(jsonPath("$.outstandingAmount").value(10000))
                .andExpect(jsonPath("$.pendingExtensionId").doesNotExist());
        getJson("/api/admin/reservations/%d/extensions".formatted(id), admin).andExpect(jsonPath("$[1].status").value("DEDUCTED"));
    }

    @Test
    void anUnpaidExtensionCanBeWithdrawn() throws Exception {
        MockHttpSession admin = admin();
        long bike = createdId(postJson("/api/admin/reservation-items", admin, """
                {"name":"자전거","price":0,"reservationType":"ITEM_RENTAL","maximumPerson":1,"isEnabled":true}
                """));
        MockHttpSession rider = member("rider");
        long id = body(postJson("/api/reservations", rider, RENT.formatted(bike, 1, DAY, DAY.plusDays(1), APPLICANT))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))).get("id").asLong();
        postJson("/api/reservations/%d/extend".formatted(id), rider, "{\"endAt\":\"%sT00:00:00\"}".formatted(DAY.plusDays(3)))
                .andExpect(jsonPath("$.pendingExtensionId").doesNotExist())
                .andExpect(jsonPath("$.endAt").value(DAY.plusDays(3) + "T00:00:00"));
        postJson("/api/reservations/%d/extension/cancel".formatted(id), rider, "")
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

        long paidBike = createdId(postJson("/api/admin/reservation-items", admin, """
                {"name":"전기 자전거","price":15000,"reservationType":"ITEM_RENTAL","maximumPerson":1,"isEnabled":true}
                """));
        long paidId = body(postJson("/api/admin/reservations?paid=true", admin, RENT.formatted(paidBike, 1, DAY, DAY.plusDays(1), APPLICANT)))
                .get("id").asLong();
        postJson("/api/admin/reservations/%d/extend".formatted(paidId), admin, "{\"endAt\":\"%sT00:00:00\"}".formatted(DAY.plusDays(2)))
                .andExpect(jsonPath("$.pendingExtensionFee").value(15000));
        postJson("/api/admin/reservations/%d/extension/cancel".formatted(paidId), admin, "")
                .andExpect(jsonPath("$.endAt").value(DAY.plusDays(1) + "T00:00:00"));
        postJson("/api/admin/reservations/%d/extend".formatted(paidId), admin, "{\"endAt\":\"%sT00:00:00\",\"paid\":true}".formatted(DAY.plusDays(2)))
                .andExpect(jsonPath("$.pendingExtensionId").doesNotExist());
    }
}

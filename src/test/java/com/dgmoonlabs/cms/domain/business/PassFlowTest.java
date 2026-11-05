package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PassFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";
    private static final String APPLICANT = "\"applicantName\":\"수강생\",\"phoneNumber\":\"010-1234-5678\"";

    private long pay(final MockHttpSession member, final long productId, final int quantity, final int amount) throws Exception {
        long orderId = body(postJson("/api/orders", member, """
                {"productId":%d,"quantity":%d,%s}
                """.formatted(productId, quantity, RECEIVER)).andExpect(status().isCreated())).get("id").asLong();
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":%d,"method":"CARD"}
                """.formatted(orderId, amount)).andExpect(status().isOk());
        return orderId;
    }

    @Test
    void countPassIsDeductedOnReservationAndRestoredOnCancel() throws Exception {
        MockHttpSession admin = admin();
        long yoga = createdId(postJson("/api/admin/programs", admin, """
                {"name":"요가","price":20000,"reservationType":"PROGRAM","isEnabled":true}
                """));
        long pilates = createdId(postJson("/api/admin/programs", admin, """
                {"name":"필라테스","price":25000,"reservationType":"PROGRAM","isEnabled":true}
                """));
        postJson("/api/admin/products", admin, """
                {"name":"요가 5회권","price":90000,"saleType":"PASS","isEnabled":true}
                """).andExpect(jsonPath("$.code").value("PASS_CONFIG_REQUIRED"));
        long pass5 = createdId(postJson("/api/admin/products", admin, """
                {"name":"요가 5회권","price":90000,"saleType":"PASS","passUnit":"COUNT","passCount":5,"passProgramId":%d,"isEnabled":true}
                """.formatted(yoga)));

        MockHttpSession member = member("yogi");
        pay(member, pass5, 1, 90000);
        long passId = body(getJson("/api/passes/me", member)
                .andExpect(jsonPath("$.content[0].remainingCount").value(5))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))).get("content").get(0).get("id").asLong();

        String book = "{\"programId\":%d,\"quantity\":%d,%s,\"passId\":%d}";
        postJson("/api/reservations", member, book.formatted(pilates, 1, APPLICANT, passId))
                .andExpect(jsonPath("$.code").value("PASS_NOT_USABLE"));

        long reservationId = body(postJson("/api/reservations", member, book.formatted(yoga, 2, APPLICANT, passId))
                .andExpect(jsonPath("$.totalPrice").value(0))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))).get("id").asLong();
        getJson("/api/passes/me", member).andExpect(jsonPath("$.content[0].remainingCount").value(3));

        postJson("/api/reservations/%d/cancel".formatted(reservationId), member, "").andExpect(status().isNoContent());
        getJson("/api/passes/me", member).andExpect(jsonPath("$.content[0].remainingCount").value(5));

        deleteJson("/api/admin/passes/" + passId, admin, null).andExpect(status().isNoContent());
        postJson("/api/reservations", member, book.formatted(yoga, 1, APPLICANT, passId))
                .andExpect(jsonPath("$.code").value("PASS_NOT_USABLE"));
    }

    @Test
    void periodPassStaysUsableUntilItExpires() throws Exception {
        MockHttpSession admin = admin();
        long yoga = createdId(postJson("/api/admin/programs", admin, """
                {"name":"요가","price":20000,"reservationType":"PROGRAM","isEnabled":true}
                """));
        long passMonth = createdId(postJson("/api/admin/products", admin, """
                {"name":"요가 한달 자유이용권","price":150000,"saleType":"PASS","passUnit":"PERIOD","passDays":30,"passProgramId":%d,"isEnabled":true}
                """.formatted(yoga)));

        MockHttpSession member = member("yogi2");
        pay(member, passMonth, 1, 150000);
        long passId = body(getJson("/api/passes/me", member)
                .andExpect(jsonPath("$.content[0].expiresAt").exists())).get("content").get(0).get("id").asLong();

        String book = "{\"programId\":%d,\"quantity\":%d,%s,\"passId\":%d}";
        postJson("/api/reservations", member, book.formatted(yoga, 3, APPLICANT, passId))
                .andExpect(jsonPath("$.totalPrice").value(0))
                .andExpect(jsonPath("$.passId").value(passId));
        postJson("/api/reservations", member, book.formatted(yoga, 1, APPLICANT, passId))
                .andExpect(jsonPath("$.totalPrice").value(0));
    }
}

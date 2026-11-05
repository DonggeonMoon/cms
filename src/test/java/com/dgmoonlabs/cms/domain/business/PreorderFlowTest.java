package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PreorderFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";

    @Test
    void preorderOnlySellsDuringTheSalePeriodAndRemembersTheShipDate() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/products", admin, """
                {"name":"한정판 피규어","price":50000,"saleType":"PREORDER","isEnabled":true}
                """).andExpect(jsonPath("$.code").value("PREORDER_SHIP_DATE_REQUIRED"));

        LocalDateTime past = LocalDateTime.now().minusDays(2);
        LocalDateTime recentPast = LocalDateTime.now().minusDays(1);
        long expired = createdId(postJson("/api/admin/products", admin, """
                {"name":"마감된 선주문","price":50000,"saleType":"PREORDER","isEnabled":true,
                 "preorderSaleStartAt":"%s","preorderSaleEndAt":"%s","preorderShipDate":"2026-12-01"}
                """.formatted(past, recentPast)));

        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
        long notYetOpen = createdId(postJson("/api/admin/products", admin, """
                {"name":"아직 안 시작한 선주문","price":50000,"saleType":"PREORDER","isEnabled":true,
                 "preorderSaleStartAt":"%s","preorderShipDate":"2026-12-01"}
                """.formatted(futureStart)));

        LocalDate shipDate = LocalDate.now().plusMonths(1);
        long figure = createdId(postJson("/api/admin/products", admin, """
                {"name":"한정판 피규어","price":50000,"saleType":"PREORDER","isEnabled":true,"preorderShipDate":"%s"}
                """.formatted(shipDate)));

        postJson("/api/admin/products", admin, """
                {"name":"기간 역전","price":50000,"saleType":"PREORDER","isEnabled":true,
                 "preorderSaleStartAt":"2026-12-10T00:00:00","preorderSaleEndAt":"2026-12-01T00:00:00","preorderShipDate":"2026-12-20"}
                """).andExpect(jsonPath("$.code").value("INVALID_PERIOD"));

        MockHttpSession member = member("preorderer");
        postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(expired, RECEIVER)).andExpect(jsonPath("$.code").value("PRODUCT_NOT_AVAILABLE"));
        postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(notYetOpen, RECEIVER)).andExpect(jsonPath("$.code").value("PRODUCT_NOT_AVAILABLE"));

        long orderId = body(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(figure, RECEIVER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.shipDate").value(shipDate.toString()))).get("id").asLong();

        getJson("/api/orders/" + orderId, member).andExpect(jsonPath("$.shipDate").value(shipDate.toString()));
    }
}

package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountingFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";

    @Test
    void salesReportAndSettlementAggregateApprovedPayments() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"회계상품","price":10000,"requiresShipping":false,"isEnabled":true}
                """));
        MockHttpSession buyer = member("buyer1");
        long orderId = createdId(postJson("/api/orders", buyer, """
                {"productId":%d,"quantity":2,%s}
                """.formatted(productId, RECEIVER)));
        postJson("/api/payments", buyer, """
                {"targetType":"ORDER","targetId":%d,"amount":20000,"method":"CARD"}
                """.formatted(orderId)).andExpect(status().isOk());

        String today = LocalDate.now().toString();
        JsonNode report = body(getJson("/api/admin/sales-report?from=" + today + "&to=" + today, admin));
        assertThat(report.get(0).get("count").asLong()).isEqualTo(1);
        assertThat(report.get(0).get("amount").asLong()).isEqualTo(20000);

        JsonNode settlement = body(getJson("/api/admin/settlements?from=" + today + "&to=" + today, admin));
        assertThat(settlement.get(0).get("method").asText()).isEqualTo("CARD");
        assertThat(settlement.get(0).get("amount").asLong()).isEqualTo(20000);

        postJson("/api/orders/" + orderId + "/cancel", buyer, "").andExpect(status().isNoContent());
        JsonNode afterCancel = body(getJson("/api/admin/sales-report?from=" + today + "&to=" + today, admin));
        assertThat(afterCancel.get(0).get("amount").asLong()).isEqualTo(0);
    }

    @Test
    void receivableTracksCreditSaleUntilFullyCollected() throws Exception {
        MockHttpSession admin = admin();
        MockHttpSession buyer = member("buyer2");
        long memberId = body(getJson("/api/admin/members?keyword=buyer2", admin)).get("content").get(0).get("id").asLong();

        long receivableId = body(postJson("/api/admin/receivables", admin, """
                {"memberId":%d,"totalAmount":50000,"note":"외상 거래"}
                """.formatted(memberId))).get("id").asLong();

        postJson("/api/admin/receivables/" + receivableId + "/collect", admin, """
                {"amount":30000}
                """).andExpect(jsonPath("$.remaining").value(20000)).andExpect(jsonPath("$.settled").value(false));

        postJson("/api/admin/receivables/" + receivableId + "/collect", admin, """
                {"amount":30000}
                """).andExpect(jsonPath("$.code").value("RECEIVABLE_OVERPAID"));

        postJson("/api/admin/receivables/" + receivableId + "/collect", admin, """
                {"amount":20000}
                """).andExpect(jsonPath("$.remaining").value(0)).andExpect(jsonPath("$.settled").value(true));

        JsonNode list = body(getJson("/api/admin/receivables?memberId=" + memberId, admin));
        assertThat(list.get("content").size()).isEqualTo(1);
    }
}

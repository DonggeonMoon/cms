package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RepairFlowTest extends ApiTestSupport {
    @Test
    void repairRequestGetsAQuoteOrderThatTheMemberPays() throws Exception {
        MockHttpSession admin = admin();
        long asFee = createdId(postJson("/api/admin/products", admin, """
                {"name":"AS 수리비","price":0,"saleType":"QUOTE","isEnabled":true}
                """));

        MockHttpSession member = member("brokenlaptop");
        long repairId = body(postJson("/api/repairs", member, """
                {"title":"노트북이 안 켜져요","description":"충전은 되는데 화면이 안 나옵니다."}
                """).andExpect(status().isOk())).get("id").asLong();

        getJson("/api/repairs/me", member)
                .andExpect(jsonPath("$.content[0].title").value("노트북이 안 켜져요"))
                .andExpect(jsonPath("$.content[0].quoteOrderId").doesNotExist());

        postJson("/api/admin/progress/REPAIR/templates", admin, """
                {"code":"RECEIVED","label":"접수","sequence":1}
                """).andExpect(status().isOk());
        postJson("/api/admin/progress/REPAIR/" + repairId + "/advance", admin, "{\"stageCode\":\"RECEIVED\"}")
                .andExpect(jsonPath("$.stageLabel").value("접수"));
        getJson("/api/progress?targetType=REPAIR&targetId=" + repairId, member)
                .andExpect(jsonPath("$.currentStageCode").value("RECEIVED"));

        postJson("/api/admin/messages?targetType=REPAIR&targetId=" + repairId, admin, """
                {"content":"점검해 보니 메인보드 교체가 필요합니다."}
                """).andExpect(status().isOk());

        long orderId = body(postJson("/api/admin/repairs/" + repairId + "/quote-order", admin, """
                {"productId":%d}
                """.formatted(asFee)))
                .get("id").asLong();
        getJson("/api/admin/repairs/" + repairId, admin).andExpect(jsonPath("$.quoteOrderId").value(orderId));
        getJson("/api/orders/me", member).andExpect(jsonPath("$.content[0].status").value("QUOTE_REQUESTED"));

        postJson("/api/admin/orders/" + orderId + "/quote", admin, """
                {"amount":120000,"message":"메인보드 교체 비용입니다."}
                """).andExpect(jsonPath("$.status").value("QUOTED"));
        postJson("/api/orders/" + orderId + "/accept-quote", member, "")
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.totalPrice").value(120000));
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":120000,"method":"CARD"}
                """.formatted(orderId)).andExpect(status().isOk());
        getJson("/api/orders/me", member).andExpect(jsonPath("$.content[0].status").value("PAID"));
    }
}

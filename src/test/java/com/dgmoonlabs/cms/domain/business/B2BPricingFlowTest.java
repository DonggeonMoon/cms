package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class B2BPricingFlowTest extends ApiTestSupport {
    @Test
    void memberWithTradeGradePaysTheNegotiatedPriceAndOthersPayTheDefault() throws Exception {
        MockHttpSession admin = admin();
        long gradeId = body(postJson("/api/admin/grades", admin, """
                {"name":"대리점","rank":5,"isDefault":false}
                """)).get("id").asLong();

        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"도매상품","price":10000,"requiresShipping":false,"isEnabled":true}
                """));

        putJson("/api/admin/products/" + productId + "/grade-prices", admin, """
                [{"gradeId":%d,"price":7000}]
                """.formatted(gradeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(7000));

        getJson("/api/admin/products/" + productId + "/grade-prices", admin)
                .andExpect(jsonPath("$[0].gradeId").value(gradeId));

        // 거래처 가격은 공개 상품 조회에 노출되지 않는다.
        getJson("/api/products/" + productId, null).andExpect(jsonPath("$.price").value(10000));

        MockHttpSession dealer = member("dealer1");
        long dealerId = body(getJson("/api/admin/members?keyword=dealer1", admin)).get("content").get(0).get("id").asLong();
        putJson("/api/admin/members/" + dealerId + "/grade", admin, """
                {"gradeId":%d}
                """.formatted(gradeId)).andExpect(status().isNoContent());

        getJson("/api/products/" + productId, dealer).andExpect(jsonPath("$.yourPrice").value(7000));

        long dealerOrderId = createdId(postJson("/api/orders", dealer, """
                {"productId":%d,"quantity":1,"receiverName":"대리점","phoneNumber":"010-1234-5678"}
                """.formatted(productId)));
        getJson("/api/orders/" + dealerOrderId, dealer).andExpect(jsonPath("$.items[0].unitPrice").value(7000))
                .andExpect(jsonPath("$.totalPrice").value(7000));

        MockHttpSession plain = member("plainbuyer1");
        JsonNode plainProduct = body(getJson("/api/products/" + productId, plain));
        assertThat(plainProduct.get("yourPrice").asInt()).isEqualTo(10000);
        long plainOrderId = createdId(postJson("/api/orders", plain, """
                {"productId":%d,"quantity":1,"receiverName":"일반회원","phoneNumber":"010-1234-5678"}
                """.formatted(productId)));
        getJson("/api/orders/" + plainOrderId, plain).andExpect(jsonPath("$.totalPrice").value(10000));
    }
}

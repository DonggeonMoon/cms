package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InternationalFlowTest extends ApiTestSupport {
    @Test
    void shippingPolicyChargesFlatFeeByCountryAndRejectsUnsupportedOnes() throws Exception {
        MockHttpSession admin = admin();
        putJson("/api/admin/shipping-policy", admin, """
                {"baseFee":3000,"freeThreshold":30000,"regionFees":[],
                 "internationalFees":[{"country":"US","fee":15000},{"country":"JP","fee":8000}]}
                """).andExpect(status().isOk())
                .andExpect(jsonPath("$.internationalFees.length()").value(2));

        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"해외배송상품","price":20000,"availableQuantity":10,"isEnabled":true}
                """));
        MockHttpSession buyer = member("globalbuyer");

        postJson("/api/orders", buyer, """
                {"productId":%d,"quantity":1,"receiverName":"구매자","phoneNumber":"010-1234-5678",
                 "address":"123 Main St","country":"US"}
                """.formatted(productId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingFee").value(15000))
                .andExpect(jsonPath("$.country").value("US"));

        postJson("/api/orders", buyer, """
                {"productId":%d,"quantity":1,"receiverName":"구매자","phoneNumber":"010-1234-5678",
                 "address":"456 Rue","country":"FR"}
                """.formatted(productId))
                .andExpect(jsonPath("$.code").value("COUNTRY_NOT_SUPPORTED"));

        postJson("/api/orders", buyer, """
                {"productId":%d,"quantity":1,"receiverName":"구매자","phoneNumber":"010-1234-5678",
                 "postalCode":"06000","address":"서울"}
                """.formatted(productId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingFee").value(3000))
                .andExpect(jsonPath("$.country").doesNotExist());
    }

    @Test
    void adminManagesExchangeRatesAndPublicListOnlyShowsEnabled() throws Exception {
        MockHttpSession admin = admin();
        putJson("/api/admin/exchange-rates", admin, """
                {"currencyCode":"usd","rate":1350.5,"enabled":true}
                """).andExpect(status().isOk())
                .andExpect(jsonPath("$.currencyCode").value("USD"));
        putJson("/api/admin/exchange-rates", admin, """
                {"currencyCode":"JPY","rate":9.1,"enabled":false}
                """).andExpect(status().isOk());

        JsonNode adminList = body(getJson("/api/admin/exchange-rates", admin));
        assertThat(adminList.size()).isEqualTo(2);

        JsonNode publicList = body(getJson("/api/exchange-rates", admin));
        assertThat(publicList.size()).isEqualTo(1);
        assertThat(publicList.get(0).get("currencyCode").asText()).isEqualTo("USD");

        putJson("/api/admin/exchange-rates", admin, """
                {"currencyCode":"USD","rate":1400,"enabled":true}
                """).andExpect(jsonPath("$.rate").value(1400));
        JsonNode afterUpdate = body(getJson("/api/admin/exchange-rates", admin));
        assertThat(afterUpdate.size()).isEqualTo(2);

        long jpyId = adminList.get(1).get("currencyCode").asText().equals("JPY") ? adminList.get(1).get("id").asLong()
                : adminList.get(0).get("id").asLong();
        deleteJson("/api/admin/exchange-rates/" + jpyId, admin, null).andExpect(status().isNoContent());
        assertThat(body(getJson("/api/admin/exchange-rates", admin)).size()).isEqualTo(1);
    }
}

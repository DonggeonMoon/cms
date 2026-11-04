package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartOrderFlowTest extends ApiTestSupport {
    private static final String RECEIVER = """
            "receiverName":"구매자","phoneNumber":"010-1234-5678","postalCode":"%s","address":"주소\"""";

    @Test
    void optionCombinationsKeepTheirOwnStockAndPrice() throws Exception {
        MockHttpSession admin = admin();
        long productId = shirt(admin);
        postJson("/api/admin/products", admin, """
                {"name":"잘못된 조합","price":1000,"isEnabled":true,
                 "optionGroups":[{"name":"색상","values":["빨강"]}],
                 "options":[{"name":"x","additionalPrice":0,"optionValues":["파랑"]}]}
                """).andExpect(jsonPath("$.code").value("INVALID_OPTION_COMBINATION"));

        JsonNode product = body(getJson("/api/products/" + productId, null));
        assertThat(product.get("optionGroups").size()).isEqualTo(2);
        JsonNode redLarge = option(product, "빨강 / L");
        assertThat(redLarge.get("stock").asInt()).isEqualTo(1);

        MockHttpSession member = member("shirtbuyer");
        postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(productId, RECEIVER.formatted("06000"))).andExpect(jsonPath("$.code").value("OPTION_REQUIRED"));
        postJson("/api/orders", member, """
                {"productId":%d,"productOptionId":%d,"quantity":2,%s}
                """.formatted(productId, redLarge.get("id").asLong(), RECEIVER.formatted("06000")))
                .andExpect(jsonPath("$.code").value("OUT_OF_STOCK"));
        postJson("/api/orders", member, """
                {"productId":%d,"productOptionId":%d,"quantity":1,%s}
                """.formatted(productId, redLarge.get("id").asLong(), RECEIVER.formatted("06000")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].unitPrice").value(12000))
                .andExpect(jsonPath("$.items[0].optionName").value("빨강 / L"));
        assertThat(option(body(getJson("/api/products/" + productId, null)), "빨강 / L").get("stock").asInt()).isZero();
    }

    @Test
    void cartCheckoutChargesShippingAndPartialCancelRefundsTheItem() throws Exception {
        MockHttpSession admin = admin();
        putJson("/api/admin/shipping-policy", admin, """
                {"baseFee":3000,"freeThreshold":30000,"regionFees":[{"name":"제주","postalCodeFrom":"63000","postalCodeTo":"63644","fee":4000}]}
                """).andExpect(status().isOk());
        long shirt = shirt(admin);
        long blueSmall = option(body(getJson("/api/products/" + shirt, null)), "파랑 / S").get("id").asLong();
        long mug = createdId(postJson("/api/admin/products", admin, """
                {"name":"머그컵","price":15000,"availableQuantity":10,"isEnabled":true}
                """));
        long ebook = createdId(postJson("/api/admin/products", admin, """
                {"name":"전자책","price":5000,"requiresShipping":false,"isEnabled":true}
                """));

        MockHttpSession member = member("cartbuyer");
        postJson("/api/cart", member, "{\"productId\":%d,\"productOptionId\":%d,\"quantity\":2}".formatted(shirt, blueSmall));
        postJson("/api/cart", member, "{\"productId\":%d,\"quantity\":1}".formatted(mug));
        postJson("/api/cart", member, "{\"productId\":%d,\"quantity\":1}".formatted(mug));
        JsonNode cart = body(getJson("/api/cart?postalCode=63100", member)
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[1].quantity").value(2))
                .andExpect(jsonPath("$.itemsAmount").value(50000))
                .andExpect(jsonPath("$.shippingFee").value(4000))
                .andExpect(jsonPath("$.shippingRegion").value("제주")));

        String ids = cart.get("items").get(0).get("id").asLong() + "," + cart.get("items").get(1).get("id").asLong();
        JsonNode order = body(postJson("/api/cart/checkout", member, """
                {"cartItemIds":[%s],%s}
                """.formatted(ids, RECEIVER.formatted("06000")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.productName").value("티셔츠 (파랑 / S) 외 1건"))
                .andExpect(jsonPath("$.shippingFee").value(0))
                .andExpect(jsonPath("$.totalPrice").value(50000)));
        getJson("/api/cart", member).andExpect(jsonPath("$.items.length()").value(0));
        long orderId = order.get("id").asLong();
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":50000,"method":"CARD"}
                """.formatted(orderId)).andExpect(jsonPath("$.status").value("APPROVED"));

        long mugItem = order.get("items").get(1).get("id").asLong();
        postJson("/api/orders/%d/items/%d/cancel".formatted(orderId, mugItem), member, "")
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.items[1].status").value("CANCELED"))
                .andExpect(jsonPath("$.items[1].refundedAmount").value(27000))
                .andExpect(jsonPath("$.shippingFee").value(3000))
                .andExpect(jsonPath("$.refundedAmount").value(27000));
        getJson("/api/products/" + mug, null).andExpect(jsonPath("$.availableQuantity").value(10));
        getJson("/api/admin/payments", admin).andExpect(jsonPath("$.content[0].refundedAmount").value(27000));

        putJson("/api/admin/orders/%d/shipment".formatted(orderId), admin, """
                {"courier":"CJ대한통운","trackingNumber":"123456789"}
                """).andExpect(jsonPath("$.status").value("SHIPPING"))
                .andExpect(jsonPath("$.trackingNumber").value("123456789"));
        postJson("/api/orders/%d/items/%d/cancel".formatted(orderId, order.get("items").get(0).get("id").asLong()), member, "")
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

        long digital = body(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,"receiverName":"구매자","phoneNumber":"010-1234-5678"}
                """.formatted(ebook)).andExpect(status().isCreated())).get("totalPrice").asLong();
        assertThat(digital).isEqualTo(5000);
        postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,"receiverName":"구매자","phoneNumber":"010-1234-5678"}
                """.formatted(mug)).andExpect(jsonPath("$.code").value("ADDRESS_REQUIRED"));
    }

    @Test
    void cancelingAPaidOrderTakesBackTheGradeItGranted() throws Exception {
        MockHttpSession admin = admin();
        long vip = body(postJson("/api/admin/grades", admin, "{\"name\":\"골드\",\"rank\":5}")).get("id").asLong();
        long pass = createdId(postJson("/api/admin/products", admin, """
                {"name":"골드 이용권","price":1000,"requiresShipping":false,"grantGradeId":%d,"isEnabled":true}
                """.formatted(vip)));
        MockHttpSession member = member("refunder");
        long orderId = body(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,"receiverName":"구매자","phoneNumber":"010-1234-5678"}
                """.formatted(pass))).get("id").asLong();
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":1000,"method":"CARD"}
                """.formatted(orderId));
        getJson("/api/members/me/grade", member).andExpect(jsonPath("$.gradeName").value("골드"));

        postJson("/api/orders/%d/cancel".formatted(orderId), member, "").andExpect(status().isNoContent());
        getJson("/api/members/me/grade", member).andExpect(jsonPath("$.gradeId").doesNotExist());
    }

    private long shirt(final MockHttpSession admin) throws Exception {
        return createdId(postJson("/api/admin/products", admin, """
                {"name":"티셔츠","price":10000,"isEnabled":true,
                 "optionGroups":[{"name":"색상","values":["빨강","파랑"]},{"name":"사이즈","values":["S","L"]}],
                 "options":[
                   {"name":"","additionalPrice":0,"optionValues":["빨강","S"],"stock":5},
                   {"name":"","additionalPrice":2000,"optionValues":["빨강","L"],"stock":1},
                   {"name":"","additionalPrice":0,"optionValues":["파랑","S"],"stock":5},
                   {"name":"","additionalPrice":2000,"optionValues":["파랑","L"],"isEnabled":false}]}
                """).andExpect(status().isCreated()));
    }

    private static JsonNode option(final JsonNode product, final String name) {
        for (JsonNode option : product.get("options")) {
            if (option.get("name").asString().equals(name)) {
                return option;
            }
        }
        throw new AssertionError("no option " + name);
    }
}

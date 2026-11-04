package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StoreOrderFlowTest extends ApiTestSupport {
    private static final String ORDERER = "\"receiverName\":\"손님\",\"phoneNumber\":\"010-1234-5678\"";

    private void pay(final MockHttpSession member, final long orderId, final int amount) throws Exception {
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":%d,"method":"CARD"}
                """.formatted(orderId, amount)).andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void pickupAndTableOrdersGetDailyNumbersAndMoveThroughTheKitchen() throws Exception {
        MockHttpSession admin = admin();
        putJson("/api/admin/store-setting", admin, "{\"pickupEnabled\":true,\"dineInEnabled\":true}")
                .andExpect(jsonPath("$.code").value("TABLE_REQUIRED"));
        putJson("/api/admin/store-setting", admin, """
                {"pickupEnabled":true,"dineInEnabled":true,"tables":["1번","2번"],"notice":"포장은 10분 걸립니다"}
                """).andExpect(jsonPath("$.openNow").value(true));
        long latte = createdId(postJson("/api/admin/products", admin,
                "{\"name\":\"라떼\",\"price\":5000,\"requiresShipping\":false,\"storeOrder\":true,\"isEnabled\":true}"));
        long beans = createdId(postJson("/api/admin/products", admin,
                "{\"name\":\"원두\",\"price\":20000,\"isEnabled\":true}"));
        getJson("/api/store-setting", null).andExpect(jsonPath("$.tables[1]").value("2번"));

        MockHttpSession guest = member("cafeguest");
        String direct = "{\"productId\":%d,\"quantity\":%d,%s,%s}";
        postJson("/api/orders", guest, direct.formatted(latte, 1, ORDERER, "\"address\":\"서울\""))
                .andExpect(jsonPath("$.code").value("STORE_ORDER_ONLY"));
        postJson("/api/orders", guest, direct.formatted(beans, 1, ORDERER, "\"fulfillmentType\":\"PICKUP\""))
                .andExpect(jsonPath("$.code").value("NOT_STORE_PRODUCT"));
        postJson("/api/orders", guest, direct.formatted(latte, 1, ORDERER, "\"fulfillmentType\":\"DINE_IN\",\"tableName\":\"9번\""))
                .andExpect(jsonPath("$.code").value("TABLE_REQUIRED"));

        long pickup = body(postJson("/api/orders", guest, direct.formatted(latte, 2, ORDERER,
                "\"fulfillmentType\":\"PICKUP\",\"requestMessage\":\"샷 추가\""))
                .andExpect(jsonPath("$.pickupNumber").value(1))
                .andExpect(jsonPath("$.shippingFee").value(0))
                .andExpect(jsonPath("$.requestMessage").value("샷 추가"))).get("id").asLong();
        long cartItem = body(postJson("/api/cart", guest, "{\"productId\":%d,\"quantity\":1}".formatted(latte)))
                .get("items").get(0).get("id").asLong();
        long table = body(postJson("/api/cart/checkout", guest, """
                {"cartItemIds":[%d],%s,"fulfillmentType":"DINE_IN","tableName":"2번"}
                """.formatted(cartItem, ORDERER))
                .andExpect(jsonPath("$.tableName").value("2번"))
                .andExpect(jsonPath("$.pickupNumber").value(2))).get("id").asLong();

        getJson("/api/admin/store-orders", admin).andExpect(jsonPath("$.length()").value(0));
        pay(guest, pickup, 10000);
        pay(guest, table, 5000);
        getJson("/api/admin/store-orders", admin)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].items[0].productName").value("라떼"));

        String statusUrl = "/api/admin/orders/%d/status".formatted(pickup);
        putJson(statusUrl, admin, "{\"status\":\"SHIPPING\"}").andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
        putJson(statusUrl, admin, "{\"status\":\"PREPARING\"}").andExpect(status().isNoContent());
        putJson(statusUrl, admin, "{\"status\":\"READY\"}").andExpect(status().isNoContent());
        getJson("/api/orders/" + pickup, guest).andExpect(jsonPath("$.status").value("READY"));
        putJson(statusUrl, admin, "{\"status\":\"COMPLETED\"}").andExpect(status().isNoContent());
        getJson("/api/admin/store-orders", admin).andExpect(jsonPath("$.length()").value(1));

        putJson("/api/admin/store-setting", admin, """
                {"pickupEnabled":true,"dineInEnabled":true,"tables":["1번"],"paused":true}
                """).andExpect(jsonPath("$.openNow").value(false));
        postJson("/api/orders", guest, direct.formatted(latte, 1, ORDERER, "\"fulfillmentType\":\"PICKUP\""))
                .andExpect(jsonPath("$.code").value("STORE_CLOSED"));
    }
}

package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LocalDeliveryFlowTest extends ApiTestSupport {
    private static final String ORDERER = "\"receiverName\":\"손님\",\"phoneNumber\":\"010-1234-5678\"";

    @Test
    void localDeliveryChecksZoneMinimumAndSlotThenChargesDeliveryFee() throws Exception {
        MockHttpSession admin = admin();
        putJson("/api/admin/store-setting", admin, "{\"localDeliveryEnabled\":true}")
                .andExpect(jsonPath("$.code").value("DELIVERY_ZONE_NOT_SUPPORTED"));
        putJson("/api/admin/store-setting", admin, """
                {"localDeliveryEnabled":true,"deliveryZones":["062"],"deliveryFee":3000,
                 "deliveryMinimumAmount":10000,"deliverySlotMinutes":30}
                """).andExpect(jsonPath("$.openNow").value(true));

        long lunchbox = createdId(postJson("/api/admin/products", admin,
                "{\"name\":\"도시락\",\"price\":8000,\"requiresShipping\":false,\"storeOrder\":true,\"isEnabled\":true}"));

        MockHttpSession guest = member("deliveryguest");
        String order = "{\"productId\":%d,\"quantity\":%d,%s,\"fulfillmentType\":\"LOCAL_DELIVERY\",%s}";

        postJson("/api/orders", guest, order.formatted(lunchbox, 1, ORDERER, "\"requestMessage\":\"문앞에 놔주세요\""))
                .andExpect(jsonPath("$.code").value("ADDRESS_REQUIRED"));

        postJson("/api/orders", guest, order.formatted(lunchbox, 1, ORDERER,
                "\"address\":\"서울\",\"postalCode\":\"07000\",\"deliverySlotAt\":\"2026-11-06T12:30:00\""))
                .andExpect(jsonPath("$.code").value("DELIVERY_ZONE_NOT_SUPPORTED"));

        postJson("/api/orders", guest, order.formatted(lunchbox, 1, ORDERER,
                "\"address\":\"서울\",\"postalCode\":\"06236\",\"deliverySlotAt\":\"2026-11-06T12:30:00\""))
                .andExpect(jsonPath("$.code").value("DELIVERY_MINIMUM_NOT_MET"));

        postJson("/api/orders", guest, order.formatted(lunchbox, 2, ORDERER,
                "\"address\":\"서울\",\"postalCode\":\"06236\",\"deliverySlotAt\":\"2026-11-06T12:15:00\""))
                .andExpect(jsonPath("$.code").value("INVALID_DELIVERY_SLOT"));

        long orderId = body(postJson("/api/orders", guest, order.formatted(lunchbox, 2, ORDERER,
                "\"address\":\"서울\",\"postalCode\":\"06236\",\"deliverySlotAt\":\"2026-11-06T12:30:00\""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingFee").value(3000))
                .andExpect(jsonPath("$.totalPrice").value(19000))
                .andExpect(jsonPath("$.pickupNumber").doesNotExist())
                .andExpect(jsonPath("$.deliverySlotAt").value("2026-11-06T12:30:00"))).get("id").asLong();

        postJson("/api/payments", guest, """
                {"targetType":"ORDER","targetId":%d,"amount":19000,"method":"CARD"}
                """.formatted(orderId)).andExpect(jsonPath("$.status").value("APPROVED"));

        getJson("/api/admin/store-orders", admin)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fulfillmentType").value("LOCAL_DELIVERY"));
    }
}

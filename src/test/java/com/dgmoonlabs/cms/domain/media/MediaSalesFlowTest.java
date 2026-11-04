package com.dgmoonlabs.cms.domain.media;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.JsonNode;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MediaSalesFlowTest extends ApiTestSupport {
    private static final String ORDERER = "\"receiverName\":\"학생\",\"phoneNumber\":\"010-1234-5678\"";

    @Test
    void paidMediaIsBoughtThroughAHiddenProductAndPlaysOnlyForBuyers() throws Exception {
        MockHttpSession admin = admin();
        long fileId = body(mockMvc.perform(multipart("/api/files").file(new MockMultipartFile("file", "lecture.mp4",
                "application/octet-stream", "0123456789".getBytes())).session(admin).header("X-CMS-Zone", "admin").with(csrf()))
                .andExpect(status().isCreated())).get("id").asLong();
        String media = "{\"title\":\"유료 강의\",\"fileId\":%d,\"durationSeconds\":100%s}";
        postJson("/api/admin/media", admin, media.formatted(fileId, ",\"paid\":true"))
                .andExpect(jsonPath("$.code").value("MEDIA_PRICE_REQUIRED"));
        long mediaId = createdId(postJson("/api/admin/media", admin,
                media.formatted(fileId, ",\"paid\":true,\"price\":30000,\"accessDays\":30")));

        JsonNode detail = body(getJson("/api/media/" + mediaId, null)
                .andExpect(jsonPath("$.paid").value(true))
                .andExpect(jsonPath("$.price").value(30000))
                .andExpect(jsonPath("$.playable").value(false))
                .andExpect(jsonPath("$.mediaUrl").doesNotExist()));
        long productId = detail.get("productId").asLong();
        getJson("/api/products", null).andExpect(jsonPath("$.page.totalElements").value(0));
        getJson("/api/products/" + productId, null).andExpect(jsonPath("$.price").value(30000));

        MockHttpSession buyer = member("student");
        String stream = "/api/media/%d/stream".formatted(mediaId);
        mockMvc.perform(get(stream).session(buyer)).andExpect(jsonPath("$.code").value("MEDIA_PURCHASE_REQUIRED"));
        postJson("/api/media/%d/start".formatted(mediaId), buyer, "{}").andExpect(jsonPath("$.code").value("MEDIA_PURCHASE_REQUIRED"));

        long orderId = body(postJson("/api/orders", buyer, "{\"productId\":%d,\"quantity\":1,%s}".formatted(productId, ORDERER))
                .andExpect(jsonPath("$.shippingFee").value(0))).get("id").asLong();
        postJson("/api/payments", buyer, """
                {"targetType":"ORDER","targetId":%d,"amount":30000,"method":"CARD"}
                """.formatted(orderId)).andExpect(jsonPath("$.status").value("APPROVED"));

        getJson("/api/media/" + mediaId, buyer)
                .andExpect(jsonPath("$.playable").value(true))
                .andExpect(jsonPath("$.accessExpiresAt").exists())
                .andExpect(jsonPath("$.mediaUrl").value(stream));
        mockMvc.perform(get(stream).session(buyer)).andExpect(status().isOk());
        postJson("/api/media/%d/start".formatted(mediaId), buyer, "{}").andExpect(status().isOk());
        mockMvc.perform(get(stream).session(member("other"))).andExpect(status().isForbidden());

        postJson("/api/orders/%d/cancel".formatted(orderId), buyer, "").andExpect(status().isNoContent());
        mockMvc.perform(get(stream).session(buyer)).andExpect(jsonPath("$.code").value("MEDIA_PURCHASE_REQUIRED"));

        putJson("/api/admin/media/" + mediaId, admin, media.formatted(fileId, ",\"paid\":false"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(stream).session(buyer)).andExpect(status().isOk());
        getJson("/api/products/" + productId, null).andExpect(status().isNotFound());
    }
}

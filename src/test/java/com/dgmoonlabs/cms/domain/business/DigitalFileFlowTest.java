package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DigitalFileFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\"";

    @Test
    void buyersDownloadWithinTheLimitAndLoseAccessOnCancel() throws Exception {
        MockHttpSession admin = admin();
        long fileId = body(mockMvc.perform(multipart("/api/files").file(new MockMultipartFile("file", "전자책.pdf",
                "application/pdf", "ebook".getBytes(StandardCharsets.UTF_8))).session(admin).header("X-CMS-Zone", "admin").with(csrf()))
                .andExpect(status().isCreated())).get("id").asLong();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"전자책","price":5000,"requiresShipping":false,"isEnabled":true,
                 "digitalFileIds":[%d],"downloadLimit":2,"downloadDays":7}
                """.formatted(fileId)));
        getJson("/api/products/" + productId, admin).andExpect(jsonPath("$.digitalFiles[0].originalName").value("전자책.pdf"));
        getJson("/api/products/" + productId, null)
                .andExpect(jsonPath("$.digitalFileCount").value(1))
                .andExpect(jsonPath("$.digitalFiles").doesNotExist());
        mockMvc.perform(get("/api/files/%d/download".formatted(fileId))).andExpect(status().isNotFound());

        MockHttpSession buyer = member("reader");
        long orderId = body(postJson("/api/orders", buyer, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(productId, RECEIVER))).get("id").asLong();
        getJson("/api/digital/me", buyer).andExpect(jsonPath("$.content.length()").value(0));
        postJson("/api/payments", buyer, """
                {"targetType":"ORDER","targetId":%d,"amount":5000,"method":"CARD"}
                """.formatted(orderId)).andExpect(jsonPath("$.status").value("APPROVED"));

        JsonNode access = body(getJson("/api/digital/me", buyer)
                .andExpect(jsonPath("$.content[0].remainingDownloads").value(2))
                .andExpect(jsonPath("$.content[0].expiresAt").exists())
                .andExpect(jsonPath("$.content[0].usable").value(true))).get("content").get(0);
        String url = access.get("files").get(0).get("downloadUrl").asString();
        mockMvc.perform(get(url).session(member("stranger"))).andExpect(status().isNotFound());
        mockMvc.perform(get(url).session(buyer)).andExpect(status().isOk()).andExpect(content().string("ebook"));
        mockMvc.perform(get(url).session(buyer)).andExpect(status().isOk());
        mockMvc.perform(get(url).session(buyer)).andExpect(jsonPath("$.code").value("DOWNLOAD_LIMIT_EXCEEDED"));
        getJson("/api/digital/me", buyer)
                .andExpect(jsonPath("$.content[0].remainingDownloads").value(0))
                .andExpect(jsonPath("$.content[0].usable").value(false));

        MockHttpSession second = member("reader2");
        long secondOrder = body(postJson("/api/orders", second, "{\"productId\":%d,\"quantity\":1,%s}".formatted(productId, RECEIVER)))
                .get("id").asLong();
        postJson("/api/payments", second, """
                {"targetType":"ORDER","targetId":%d,"amount":5000,"method":"CARD"}
                """.formatted(secondOrder)).andExpect(status().is2xxSuccessful());
        postJson("/api/orders/%d/cancel".formatted(secondOrder), second, "").andExpect(status().isNoContent());
        String secondUrl = body(getJson("/api/digital/me", second).andExpect(jsonPath("$.content[0].revokedAt").exists()))
                .get("content").get(0).get("files").get(0).get("downloadUrl").asString();
        mockMvc.perform(get(secondUrl).session(second)).andExpect(jsonPath("$.code").value("DOWNLOAD_REVOKED"));
    }
}

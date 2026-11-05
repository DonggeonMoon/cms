package com.dgmoonlabs.cms.domain.board;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaidArticleFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";

    @Test
    void readingAPaidArticleRequiresPaymentAndCancelRevokesIt() throws Exception {
        MockHttpSession admin = admin();
        long boardId = createdId(postJson("/api/admin/boards", admin, """
                {"name":"유료글판%d","type":"NORMAL","maximumFileCount":5,"pageSize":10,
                 "isMemberAccessible":true,"isMemberReadable":true,"supportsPaidContent":true}
                """.formatted(System.nanoTime())).andExpect(status().isCreated()));

        long articleId = createdId(postJson("/api/boards/" + boardId + "/articles", admin, """
                {"title":"유료 분석글","contentHtml":"<p>비밀 본문</p>","paid":true,"price":5000}
                """).andExpect(status().isCreated()));

        Long productId = body(getJson("/api/boards/" + boardId + "/articles/" + articleId, admin)).get("productId").asLong();

        MockHttpSession member = member("reader");
        getJson("/api/boards/" + boardId + "/articles/" + articleId, member)
                .andExpect(jsonPath("$.paid").value(true))
                .andExpect(jsonPath("$.purchased").value(false))
                .andExpect(jsonPath("$.contentHtml").doesNotExist());

        long orderId = createdId(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(productId, RECEIVER)));
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":5000,"method":"CARD"}
                """.formatted(orderId)).andExpect(status().isOk());

        getJson("/api/boards/" + boardId + "/articles/" + articleId, member)
                .andExpect(jsonPath("$.purchased").value(true))
                .andExpect(jsonPath("$.contentHtml").value("<p>비밀 본문</p>"));

        postJson("/api/orders/" + orderId + "/cancel", member, "").andExpect(status().isNoContent());

        getJson("/api/boards/" + boardId + "/articles/" + articleId, member)
                .andExpect(jsonPath("$.purchased").value(false))
                .andExpect(jsonPath("$.contentHtml").doesNotExist());
    }
}

package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProgressAndMessageFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";

    @Test
    void customOrderProgressAndMessagesAreVisibleOnlyToTheOwner() throws Exception {
        MockHttpSession admin = admin();
        long mug = createdId(postJson("/api/admin/products", admin, """
                {"name":"각인 머그컵","price":15000,"isEnabled":true}
                """));

        postJson("/api/admin/progress/ORDER/templates", admin, """
                {"code":"RECEIVED","label":"접수","sequence":1}
                """).andExpect(status().isOk());
        postJson("/api/admin/progress/ORDER/templates", admin, """
                {"code":"RECEIVED","label":"접수(중복)","sequence":1}
                """).andExpect(jsonPath("$.code").value("DUPLICATE_PROGRESS_STAGE_CODE"));
        postJson("/api/admin/progress/ORDER/templates", admin, """
                {"code":"SHIPPED","label":"출고 완료","sequence":2}
                """).andExpect(status().isOk());

        MockHttpSession member = member("orderbuyer");
        MockHttpSession stranger = member("stranger");
        long orderId = body(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(mug, RECEIVER)).andExpect(status().isCreated())).get("id").asLong();

        getJson("/api/progress?targetType=ORDER&targetId=" + orderId, member)
                .andExpect(jsonPath("$.templates.length()").value(2))
                .andExpect(jsonPath("$.currentStageCode").doesNotExist());
        postJson("/api/admin/progress/ORDER/" + orderId + "/advance", admin, "{\"stageCode\":\"UNKNOWN\"}")
                .andExpect(jsonPath("$.code").value("INVALID_PROGRESS_STAGE"));
        postJson("/api/admin/progress/ORDER/" + orderId + "/advance", admin, "{\"stageCode\":\"RECEIVED\",\"memo\":\"각인 문구 확인 중\"}")
                .andExpect(jsonPath("$.stageLabel").value("접수"));

        getJson("/api/progress?targetType=ORDER&targetId=" + orderId, member)
                .andExpect(jsonPath("$.currentStageCode").value("RECEIVED"))
                .andExpect(jsonPath("$.history[0].memo").value("각인 문구 확인 중"));
        getJson("/api/progress?targetType=ORDER&targetId=" + orderId, stranger).andExpect(status().isNotFound());

        postJson("/api/admin/messages?targetType=ORDER&targetId=" + orderId, admin, """
                {"content":"각인할 문구를 알려주세요."}
                """).andExpect(jsonPath("$.senderType").value("ADMIN"));
        postJson("/api/messages?targetType=ORDER&targetId=" + orderId, member, """
                {"content":"\\"사랑해요\\" 로 부탁드려요."}
                """).andExpect(status().isOk());
        postJson("/api/messages?targetType=ORDER&targetId=" + orderId, stranger, """
                {"content":"몰래 보기"}
                """).andExpect(status().isNotFound());

        getJson("/api/messages?targetType=ORDER&targetId=" + orderId, member)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].senderType").value("ADMIN"))
                .andExpect(jsonPath("$[1].senderType").value("MEMBER"));
        getJson("/api/admin/messages?targetType=ORDER&targetId=" + orderId, admin)
                .andExpect(jsonPath("$.length()").value(2));
        getJson("/api/messages?targetType=ORDER&targetId=" + orderId, stranger).andExpect(status().isNotFound());
    }
}

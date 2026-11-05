package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.domain.business.order.entity.CartItem;
import com.dgmoonlabs.cms.domain.business.order.repository.CartItemRepository;
import com.dgmoonlabs.cms.domain.business.marketing.service.CartAbandonmentService;
import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MarketingFlowTest extends ApiTestSupport {
    @Autowired
    private CartAbandonmentService cartAbandonmentService;
    @Autowired
    private CartItemRepository cartItemRepository;

    /** 마케팅성 알림(대상 발송·장바구니 리마인드)은 마케팅 활용·수신 동의가 있어야 간다. */
    private void agreeToMarketing(final MockHttpSession session, final String username) throws Exception {
        putJson("/api/members/me", session, """
                {"name":"%s님","isMailReceptionAgreed":true,"isSmsReceptionAgreed":true,"isMarketingUsageAgreed":true}
                """.formatted(username)).andExpect(status().isNoContent());
    }

    @Test
    void campaignSendsToMembersInTargetGradeOnlyAndRecordsHistory() throws Exception {
        MockHttpSession admin = admin();
        long gradeId = body(postJson("/api/admin/grades", admin, """
                {"name":"VIP","rank":10,"isDefault":false}
                """)).get("id").asLong();
        MockHttpSession plain = member("plainmember");
        agreeToMarketing(plain, "plainmember");
        MockHttpSession vip = member("vipmember");
        agreeToMarketing(vip, "vipmember");
        long vipMemberId = body(getJson("/api/admin/members?keyword=vipmember", admin)).get("content").get(0).get("id").asLong();
        putJson("/api/admin/members/" + vipMemberId + "/grade", admin, """
                {"gradeId":%d}
                """.formatted(gradeId)).andExpect(status().isNoContent());

        postJson("/api/admin/marketing/campaigns", admin, """
                {"title":"VIP 전용 혜택","content":"VIP 회원만 받는 알림입니다.","targetGradeId":%d,"channels":["IN_APP"]}
                """.formatted(gradeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipientCount").value(1));

        getJson("/api/members/me/notifications", vip).andExpect(jsonPath("$.content[0].title").value("VIP 전용 혜택"));

        JsonNode history = body(getJson("/api/admin/marketing/campaigns", admin));
        assertThat(history.get("content").size()).isEqualTo(1);

        postJson("/api/admin/marketing/campaigns", admin, """
                {"title":"전체 공지","content":"모든 회원에게 보냅니다.","channels":["IN_APP"]}
                """).andExpect(jsonPath("$.recipientCount").value(2));
    }

    @Test
    void cartAbandonmentRemindsOncePerMemberAndNotTwice() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"마케팅상품","price":10000,"isEnabled":true}
                """));
        MockHttpSession buyer = member("abandonbuyer");
        agreeToMarketing(buyer, "abandonbuyer");
        postJson("/api/cart", buyer, "{\"productId\":%d,\"quantity\":1}".formatted(productId)).andExpect(status().isOk());

        assertThat(cartAbandonmentService.remind(LocalDateTime.now())).isZero();

        CartItem item = cartItemRepository.findAll().stream().filter(ci -> ci.getProductId().equals(productId)).findFirst().orElseThrow();
        assertThat(cartAbandonmentService.remind(LocalDateTime.now().plusHours(25))).isEqualTo(1);
        getJson("/api/members/me/notifications", buyer).andExpect(jsonPath("$.content[0].title").value("장바구니에 담긴 상품이 있어요"));

        assertThat(cartAbandonmentService.remind(LocalDateTime.now().plusHours(50))).isZero();
    }

    @Test
    void referrerEarnsPointsWhenReferredMemberPaysForTheFirstTime() throws Exception {
        MockHttpSession admin = admin();
        putJson("/api/admin/points/policy", admin, """
                {"isEnabled":true,"earnRate":0,"expireDays":365,"minUsePoints":0,"maxUseRate":100,"signupPoints":0,"referralPoints":500}
                """).andExpect(status().isOk());

        member("referrer1");
        long referrerId = body(getJson("/api/admin/members?keyword=referrer1", admin)).get("content").get(0).get("id").asLong();

        postJson("/api/members", null, """
                {"username":"referred1","password":"password123","passwordCheck":"password123",
                 "isPrivacyConsentAgreed":true,"name":"referred1님","referredBy":%d}
                """.formatted(referrerId)).andExpect(status().isCreated());
        MockHttpSession referred = login("referred1", "password123", "MEMBER");

        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"추천상품","price":20000,"requiresShipping":false,"isEnabled":true}
                """));
        long orderId = createdId(postJson("/api/orders", referred, """
                {"productId":%d,"quantity":1,"receiverName":"구매자","phoneNumber":"010-1234-5678"}
                """.formatted(productId)));
        postJson("/api/payments", referred, """
                {"targetType":"ORDER","targetId":%d,"amount":20000,"method":"CARD"}
                """.formatted(orderId)).andExpect(status().isOk());

        getJson("/api/admin/members/" + referrerId + "/points", admin).andExpect(jsonPath("$.balance").value(500));

        long orderId2 = createdId(postJson("/api/orders", referred, """
                {"productId":%d,"quantity":1,"receiverName":"구매자","phoneNumber":"010-1234-5678"}
                """.formatted(productId)));
        postJson("/api/payments", referred, """
                {"targetType":"ORDER","targetId":%d,"amount":20000,"method":"CARD"}
                """.formatted(orderId2)).andExpect(status().isOk());
        getJson("/api/admin/members/" + referrerId + "/points", admin).andExpect(jsonPath("$.balance").value(500));
    }
}

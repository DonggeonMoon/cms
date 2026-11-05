package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.domain.business.order.service.FundingService;
import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FundingFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";

    @Autowired
    private FundingService fundingService;

    @Test
    void campaignThatMissesItsGoalRefundsEveryBacker() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/products", admin, """
                {"name":"보드게임 펀딩","price":30000,"saleType":"FUNDING","isEnabled":true}
                """).andExpect(jsonPath("$.code").value("FUNDING_GOAL_REQUIRED"));

        LocalDateTime deadline = LocalDateTime.now().plusDays(7);
        long game = createdId(postJson("/api/admin/products", admin, """
                {"name":"보드게임 펀딩","price":30000,"saleType":"FUNDING","goalAmount":100000,
                 "fundingDeadline":"%s","isEnabled":true}
                """.formatted(deadline)));

        MockHttpSession backer1 = member("backer1");
        long order1 = body(postJson("/api/orders", backer1, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(game, RECEIVER))).get("id").asLong();
        postJson("/api/payments", backer1, """
                {"targetType":"ORDER","targetId":%d,"amount":30000,"method":"CARD"}
                """.formatted(order1)).andExpect(status().isOk());

        getJson("/api/products/" + game, admin)
                .andExpect(jsonPath("$.funding.raisedAmount").value(30000))
                .andExpect(jsonPath("$.funding.supporterCount").value(1))
                .andExpect(jsonPath("$.funding.succeeded").value(false));

        fundingService.resolveDue(deadline.minusDays(1));
        getJson("/api/orders/" + order1, backer1).andExpect(jsonPath("$.status").value("PAID"));

        fundingService.resolveDue(deadline.plusSeconds(1));
        getJson("/api/orders/" + order1, backer1).andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void pledgingAfterTheDeadlineHasAlreadyPassedIsRejected() throws Exception {
        MockHttpSession admin = admin();
        long game = createdId(postJson("/api/admin/products", admin, """
                {"name":"마감된 펀딩","price":30000,"saleType":"FUNDING","goalAmount":100000,
                 "fundingDeadline":"%s","isEnabled":true}
                """.formatted(LocalDateTime.now().minusMinutes(1))));

        MockHttpSession backer = member("latebacker");
        postJson("/api/orders", backer, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(game, RECEIVER)).andExpect(jsonPath("$.code").value("PRODUCT_NOT_AVAILABLE"));
    }

    @Test
    void campaignThatReachesItsGoalKeepsThePayments() throws Exception {
        MockHttpSession admin = admin();
        LocalDateTime deadline = LocalDateTime.now().plusDays(7);
        long game = createdId(postJson("/api/admin/products", admin, """
                {"name":"보드게임 성공 펀딩","price":50000,"saleType":"FUNDING","goalAmount":50000,
                 "fundingDeadline":"%s","isEnabled":true}
                """.formatted(deadline)));

        MockHttpSession backer = member("backer2");
        long order = body(postJson("/api/orders", backer, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(game, RECEIVER))).get("id").asLong();
        postJson("/api/payments", backer, """
                {"targetType":"ORDER","targetId":%d,"amount":50000,"method":"CARD"}
                """.formatted(order)).andExpect(status().isOk());

        fundingService.resolveDue(deadline.plusSeconds(1));
        getJson("/api/orders/" + order, backer).andExpect(jsonPath("$.status").value("PAID"));
        getJson("/api/products/" + game, admin).andExpect(jsonPath("$.funding.succeeded").value(true));
    }
}

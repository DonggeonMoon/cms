package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewFlowTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\",\"address\":\"서울\"";
    private static final String APPLICANT = "\"applicantName\":\"수강생\",\"phoneNumber\":\"010-1234-5678\"";

    private long buyProduct(final MockHttpSession member, final long productId) throws Exception {
        long orderId = createdId(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s}
                """.formatted(productId, RECEIVER)));
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":10000,"method":"CARD"}
                """.formatted(orderId)).andExpect(status().isOk());
        return orderId;
    }

    @Test
    void productReviewRequiresPurchaseAndCannotBeSubmittedTwice() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"머그컵","price":10000,"availableQuantity":5,"isEnabled":true}
                """));

        MockHttpSession member = member("reviewer1");
        postJson("/api/products/%d/reviews".formatted(productId), member, """
                {"rating":5,"title":"좋아요","contentHtml":"아주 마음에 들어요"}
                """).andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        buyProduct(member, productId);
        postJson("/api/products/%d/reviews".formatted(productId), member, """
                {"rating":5,"title":"좋아요","contentHtml":"아주 마음에 들어요"}
                """).andExpect(status().isCreated());
        getJson("/api/products/%d/reviews".formatted(productId), null)
                .andExpect(jsonPath("$.content[0].title").value("좋아요"));

        postJson("/api/products/%d/reviews".formatted(productId), member, """
                {"rating":4,"title":"또 씀","contentHtml":"다시 리뷰"}
                """).andExpect(jsonPath("$.code").value("ALREADY_SUBMITTED"));
    }

    @Test
    void reservationItemReviewRequiresConfirmedReservation() throws Exception {
        MockHttpSession admin = admin();
        long itemId = createdId(postJson("/api/admin/reservation-items", admin, """
                {"name":"요가","price":0,"reservationType":"PROGRAM","isEnabled":true}
                """));

        MockHttpSession member = member("reviewer2");
        postJson("/api/reservation-items/%d/reviews".formatted(itemId), member, """
                {"rating":5,"title":"좋은 수업","contentHtml":"만족합니다"}
                """).andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        postJson("/api/reservations", member, """
                {"reservationItemId":%d,"quantity":1,%s}
                """.formatted(itemId, APPLICANT)).andExpect(jsonPath("$.status").value("CONFIRMED"));

        postJson("/api/reservation-items/%d/reviews".formatted(itemId), member, """
                {"rating":5,"title":"좋은 수업","contentHtml":"만족합니다"}
                """).andExpect(status().isCreated());
        getJson("/api/reservation-items/%d/reviews".formatted(itemId), null)
                .andExpect(jsonPath("$.content[0].title").value("좋은 수업"));
    }

    @Test
    void reviewPolicyRejectsContentShorterThanConfiguredMinimum() throws Exception {
        MockHttpSession admin = admin();
        getJson("/api/review-policy", null).andExpect(jsonPath("$.minimumContentLength").value(0));

        putJson("/api/admin/review-policy", admin, "{\"minimumContentLength\":5}")
                .andExpect(jsonPath("$.minimumContentLength").value(5));
        getJson("/api/review-policy", null).andExpect(jsonPath("$.minimumContentLength").value(5));

        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"키링","price":10000,"availableQuantity":5,"isEnabled":true}
                """));
        MockHttpSession member = member("reviewer3");
        buyProduct(member, productId);

        postJson("/api/products/%d/reviews".formatted(productId), member, """
                {"rating":3,"title":"짧은 리뷰","contentHtml":"짧음"}
                """).andExpect(jsonPath("$.code").value("REVIEW_CONTENT_TOO_SHORT"));

        postJson("/api/products/%d/reviews".formatted(productId), member, """
                {"rating":3,"title":"충분한 리뷰","contentHtml":"다섯 글자 넘음"}
                """).andExpect(status().isCreated());
    }

    @Test
    void reviewCanBeDeletedByAuthorOrAdminButNotByOtherMembers() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"엽서","price":10000,"availableQuantity":5,"isEnabled":true}
                """));
        MockHttpSession author = member("author1");
        buyProduct(author, productId);
        long reviewId = createdId(postJson("/api/products/%d/reviews".formatted(productId), author, """
                {"rating":5,"title":"리뷰","contentHtml":"삭제 테스트용 리뷰"}
                """));

        MockHttpSession stranger = member("stranger1");
        deleteJson("/api/reviews/" + reviewId, stranger, null).andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        deleteJson("/api/reviews/" + reviewId, admin, null).andExpect(status().isNoContent());
    }
}

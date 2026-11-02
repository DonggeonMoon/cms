package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ShopFlowTest extends ApiTestSupport {
    @Test
    void orderPayReviewAndCancel() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"머그컵","price":10000,"availableQuantity":5,"isEnabled":true,
                 "options":[{"name":"대형","additionalPrice":2000}]}
                """).andExpect(status().isCreated()));
        JsonNode product = body(getJson("/api/products/" + productId, null).andExpect(status().isOk()));
        long optionId = product.get("options").get(0).get("id").asLong();
        long discountId = createdId(postJson("/api/admin/discounts", admin, """
                {"productId":%d,"name":"오픈 기념","rate":10,"isUsing":true}
                """.formatted(productId)).andExpect(status().isCreated()));

        MockHttpSession buyer = member("buyer");
        JsonNode order = body(postJson("/api/orders", buyer, """
                {"productId":%d,"productOptionId":%d,"quantity":2,"discountId":%d,
                 "receiverName":"구매자","phoneNumber":"010-0000-0000","address":"서울"}
                """.formatted(productId, optionId, discountId)).andExpect(status().isCreated()));
        long orderId = order.get("id").asLong();
        assertThat(order.get("totalPrice").decimalValue()).isEqualByComparingTo("21600");
        assertThat(order.get("status").asString()).isEqualTo("PENDING_PAYMENT");
        getJson("/api/products/" + productId, null).andExpect(jsonPath("$.availableQuantity").value(3));

        postJson("/api/products/" + productId + "/reviews", buyer, """
                {"rating":5,"title":"좋아요"}
                """).andExpect(status().isForbidden());

        postJson("/api/payments", buyer, """
                {"targetType":"ORDER","targetId":%d,"amount":100,"method":"CARD"}
                """.formatted(orderId)).andExpect(jsonPath("$.code").value("PAYMENT_AMOUNT_MISMATCH"));
        postJson("/api/payments", buyer, """
                {"targetType":"ORDER","targetId":%d,"amount":21600,"method":"CARD"}
                """.formatted(orderId))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.transactionId").exists());
        getJson("/api/orders/" + orderId, buyer).andExpect(jsonPath("$.status").value("PAID"));

        postJson("/api/products/" + productId + "/reviews", buyer, """
                {"rating":4,"title":"좋아요","contentHtml":"<p>튼튼함</p>"}
                """).andExpect(status().isCreated());
        getJson("/api/products/" + productId, null).andExpect(jsonPath("$.averageRating").value(4.0));

        MockHttpSession other = member("other");
        getJson("/api/orders/" + orderId, other).andExpect(status().isNotFound());

        postJson("/api/orders/" + orderId + "/cancel", buyer, "").andExpect(status().isNoContent());
        getJson("/api/orders/" + orderId, buyer).andExpect(jsonPath("$.status").value("CANCELED"));
        getJson("/api/products/" + productId, null).andExpect(jsonPath("$.availableQuantity").value(5));
        getJson("/api/admin/payments", admin).andExpect(jsonPath("$.content[0].status").value("CANCELED"));
    }

    @Test
    void stockAndAnonymousOrdersAreRejected() throws Exception {
        MockHttpSession admin = admin();
        long productId = createdId(postJson("/api/admin/products", admin, """
                {"name":"한정판","price":5000,"availableQuantity":1,"isEnabled":true}
                """).andExpect(status().isCreated()));

        postJson("/api/orders", null, """
                {"productId":%d,"quantity":1,"receiverName":"a","phoneNumber":"1"}
                """.formatted(productId)).andExpect(status().isUnauthorized());

        MockHttpSession buyer = member("stock");
        postJson("/api/orders", buyer, """
                {"productId":%d,"quantity":2,"receiverName":"a","phoneNumber":"1"}
                """.formatted(productId)).andExpect(jsonPath("$.code").value("OUT_OF_STOCK"));
    }

    @Test
    void confirmedParticipantsReviewProgramsSeparatelyFromProducts() throws Exception {
        MockHttpSession admin = admin();
        long programId = createdId(postJson("/api/admin/programs", admin, """
                {"name":"요가 교실","price":0,"maximumPerson":10,"isEnabled":true}
                """).andExpect(status().isCreated()));
        MockHttpSession member = member("yogaone");

        postJson("/api/programs/" + programId + "/reviews", member, """
                {"rating":5,"title":"예약 전"}
                """).andExpect(status().isForbidden());
        postJson("/api/reservations", member, """
                {"programId":%d,"quantity":1,"applicantName":"요가","phoneNumber":"010"}
                """.formatted(programId)).andExpect(status().isCreated());

        postJson("/api/programs/" + programId + "/reviews", member, """
                {"rating":4,"title":"좋았어요","contentHtml":"<p>강사님 친절</p>"}
                """).andExpect(status().isCreated());
        postJson("/api/programs/" + programId + "/reviews", member, """
                {"rating":1,"title":"두 번째"}
                """).andExpect(jsonPath("$.code").value("ALREADY_SUBMITTED"));

        getJson("/api/programs/" + programId + "/reviews", null)
                .andExpect(jsonPath("$.content[0].title").value("좋았어요"))
                .andExpect(jsonPath("$.content[0].targetType").value("PROGRAM"));
        getJson("/api/programs/" + programId, null).andExpect(jsonPath("$.averageRating").value(4.0));

        getJson("/api/admin/reviews?targetType=PROGRAM", admin)
                .andExpect(jsonPath("$.content[0].programId").value(programId));
        getJson("/api/admin/reviews?targetType=PRODUCT", admin).andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    void reservationsRespectCapacityAndPeriod() throws Exception {
        MockHttpSession admin = admin();
        long freeProgram = createdId(postJson("/api/admin/programs", admin, """
                {"name":"무료 강좌","price":0,"maximumPerson":2,"isEnabled":true}
                """).andExpect(status().isCreated()));
        long closedProgram = createdId(postJson("/api/admin/programs", admin, """
                {"name":"마감 강좌","price":0,"maximumPerson":10,"isEnabled":true,"applicationEndDateTime":"%s"}
                """.formatted(LocalDateTime.now().minusDays(1))).andExpect(status().isCreated()));

        MockHttpSession member = member("student");
        JsonNode reservation = body(postJson("/api/reservations", member, """
                {"programId":%d,"quantity":2,"applicantName":"학생","phoneNumber":"010"}
                """.formatted(freeProgram)).andExpect(status().isCreated()));
        assertThat(reservation.get("status").asString()).isEqualTo("CONFIRMED");

        postJson("/api/reservations", member, """
                {"programId":%d,"quantity":1,"applicantName":"학생","phoneNumber":"010"}
                """.formatted(freeProgram)).andExpect(jsonPath("$.code").value("PROGRAM_FULL"));
        postJson("/api/reservations", member, """
                {"programId":%d,"quantity":1,"applicantName":"학생","phoneNumber":"010"}
                """.formatted(closedProgram)).andExpect(jsonPath("$.code").value("PROGRAM_NOT_OPEN"));

        postJson("/api/reservations/" + reservation.get("id").asLong() + "/cancel", member, "").andExpect(status().isNoContent());
        getJson("/api/programs/" + freeProgram, null).andExpect(jsonPath("$.availablePerson").value(2));
    }
}

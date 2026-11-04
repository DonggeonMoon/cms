package com.dgmoonlabs.cms.domain.common.grade;

import com.dgmoonlabs.cms.domain.common.grade.service.GradeService;
import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GradeApiTest extends ApiTestSupport {
    private static final String RECEIVER = "\"receiverName\":\"구매자\",\"phoneNumber\":\"010-1234-5678\"";

    @Autowired
    private GradeService gradeService;

    @Test
    void gradedContentIsLockedUntilTheMemberIsAssignedAHighEnoughGrade() throws Exception {
        MockHttpSession admin = admin();
        long basic = grade(admin, "일반", 0, true);
        long premium = grade(admin, "프리미엄", 10, false);
        long contentId = createdId(postJson("/api/admin/contents", admin, """
                {"title":"회원 전용 글","contentHtml":"<p>비밀</p>","status":"PUBLISHED","accessGradeId":%d}
                """.formatted(premium)));

        getJson("/api/contents/" + contentId, null).andExpect(jsonPath("$.code").value("GRADE_REQUIRED"));
        MockHttpSession member = member("gradeuser");
        getJson("/api/contents/" + contentId, member)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("프리미엄 등급 이상 회원만 이용할 수 있습니다."));
        getJson("/api/members/me/grade", member).andExpect(jsonPath("$.gradeId").value(basic));

        long memberId = body(getJson("/api/members/me", member)).get("id").asLong();
        putJson("/api/admin/members/" + memberId + "/grade", admin, """
                {"gradeId":%d,"expiresAt":"%s"}
                """.formatted(premium, LocalDateTime.now().plusDays(3).withNano(0))).andExpect(status().isNoContent());
        getJson("/api/contents/" + contentId, member).andExpect(status().isOk());
        getJson("/api/admin/members?keyword=gradeuser", admin).andExpect(jsonPath("$.content[0].gradeName").value("프리미엄"));

        assertThat(gradeService.expireDue(LocalDateTime.now().plusDays(4))).isEqualTo(1);
        getJson("/api/contents/" + contentId, member).andExpect(jsonPath("$.code").value("GRADE_REQUIRED"));
        getJson("/api/admin/members/" + memberId + "/grade/history", admin)
                .andExpect(jsonPath("$[0].reason").value("EXPIRED"))
                .andExpect(jsonPath("$[1].reason").value("MANUAL"));

        deleteJson("/api/admin/grades/" + premium, admin, null).andExpect(jsonPath("$.code").value("GRADE_IN_USE"));
    }

    @Test
    void buyingAGradeProductGrantsTheGradeAndUnlocksGatedProducts() throws Exception {
        MockHttpSession admin = admin();
        long vip = grade(admin, "VIP", 20, false);
        long pass = createdId(postJson("/api/admin/products", admin, """
                {"name":"VIP 30일권","price":10000,"grantGradeId":%d,"grantGradeDays":30,"isEnabled":true}
                """.formatted(vip)));
        long gated = createdId(postJson("/api/admin/products", admin, """
                {"name":"VIP 전용 상품","price":5000,"accessGradeId":%d,"isEnabled":true}
                """.formatted(vip)));

        MockHttpSession member = member("vipbuyer");
        postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s,"address":"서울"}
                """.formatted(gated, RECEIVER)).andExpect(jsonPath("$.code").value("GRADE_REQUIRED"));

        long orderId = body(postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s,"address":"서울"}
                """.formatted(pass, RECEIVER))).get("id").asLong();
        postJson("/api/payments", member, """
                {"targetType":"ORDER","targetId":%d,"amount":10000,"method":"CARD"}
                """.formatted(orderId)).andExpect(jsonPath("$.status").value("APPROVED"));

        getJson("/api/members/me/grade", member)
                .andExpect(jsonPath("$.gradeName").value("VIP"))
                .andExpect(jsonPath("$.expiresAt").exists());
        postJson("/api/orders", member, """
                {"productId":%d,"quantity":1,%s,"address":"서울"}
                """.formatted(gated, RECEIVER)).andExpect(status().isCreated());
    }

    @Test
    void boardWriteGradeBlocksLowerMembers() throws Exception {
        MockHttpSession admin = admin();
        long writer = grade(admin, "작가", 5, false);
        long boardId = createdId(postJson("/api/admin/boards", admin, """
                {"name":"작가 게시판","type":"NORMAL","isMemberAccessible":true,"isMemberReadable":true,"isMemberWritable":true,
                 "isGuestAccessible":true,"isGuestReadable":true,"maximumFileCount":0,"pageSize":10,"writeGradeId":%d}
                """.formatted(writer)));
        MockHttpSession member = member("reader");
        getJson("/api/boards/" + boardId, member).andExpect(jsonPath("$.permissions.canWrite").value(false));
        postJson("/api/boards/" + boardId + "/articles", member, """
                {"title":"글","contentHtml":"<p>내용</p>"}
                """).andExpect(jsonPath("$.code").value("GRADE_REQUIRED"));
    }

    private long grade(final MockHttpSession admin, final String name, final int rank, final boolean isDefault) throws Exception {
        return body(postJson("/api/admin/grades", admin, """
                {"name":"%s","rank":%d,"isDefault":%b}
                """.formatted(name, rank, isDefault)).andExpect(status().isOk())).get("id").asLong();
    }
}

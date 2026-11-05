package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerRecordFlowTest extends ApiTestSupport {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void staffKeepTreatmentHistoryPerMemberAndShareOnlyWhatTheyChoose() throws Exception {
        MockHttpSession admin = admin();
        long programId = createdId(postJson("/api/admin/programs", admin, """
                {"name":"두피 관리","price":0,"reservationType":"PROGRAM","isEnabled":true}
                """));
        MockHttpSession member = member("client");
        long memberId = body(getJson("/api/members/me", member)).get("id").asLong();
        long reservationId = body(postJson("/api/reservations", member, """
                {"programId":%d,"quantity":1,"applicantName":"고객","phoneNumber":"010-1234-5678"}
                """.formatted(programId))).get("id").asLong();
        MockHttpSession stranger = member("stranger");
        long strangerId = body(getJson("/api/members/me", stranger)).get("id").asLong();

        String record = """
                {"memberId":%d,"reservationId":%s,"recordedOn":"%s","category":"시술","title":"%s","content":"%s","sharedWithMember":%s}
                """;
        postJson("/api/admin/customer-records", admin, record.formatted(strangerId, reservationId, LocalDate.now(), "1회차", "x", false))
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_OF_MEMBER"));
        long shared = body(postJson("/api/admin/customer-records", admin,
                record.formatted(memberId, reservationId, LocalDate.now(), "두피 1회차", "건조함, 다음엔 보습 앰플", true))
                .andExpect(jsonPath("$.authorName").value("관리자"))).get("id").asLong();
        postJson("/api/admin/customer-records", admin,
                record.formatted(memberId, "null", LocalDate.now().minusDays(30), "첫 상담", "알레르기 확인 필요", false))
                .andExpect(status().isOk());

        String stored = jdbcTemplate.queryForObject("select content from customer_record where id = ?", String.class, shared);
        assertThat(stored).doesNotContain("건조함");

        getJson("/api/admin/customer-records?memberId=" + memberId, admin)
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].content").value("건조함, 다음엔 보습 앰플"));
        getJson("/api/admin/customer-records?reservationId=" + reservationId, admin).andExpect(jsonPath("$.content.length()").value(1));
        getJson("/api/admin/customer-records?keyword=상담", admin).andExpect(jsonPath("$.content.length()").value(1));

        getJson("/api/customer-records/me", member)
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("두피 1회차"))
                .andExpect(jsonPath("$.content[0].authorName").doesNotExist());
        getJson("/api/customer-records/me", stranger).andExpect(jsonPath("$.content.length()").value(0));
        getJson("/api/admin/customer-records", member).andExpect(status().isUnauthorized());

        putJson("/api/admin/customer-records/" + shared, admin,
                record.formatted(memberId, reservationId, LocalDate.now(), "두피 1회차", "수정", false))
                .andExpect(jsonPath("$.sharedWithMember").value(false));
        getJson("/api/customer-records/me", member).andExpect(jsonPath("$.content.length()").value(0));
        deleteJson("/api/admin/customer-records/" + shared, admin, null).andExpect(status().isNoContent());
        getJson("/api/admin/customer-records?memberId=" + memberId, admin).andExpect(jsonPath("$.content.length()").value(1));
    }
}

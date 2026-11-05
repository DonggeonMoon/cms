package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VoteFlowTest extends ApiTestSupport {
    @Test
    void membersVoteOnceAndResultsRevealOnlyAfterClosing() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/votes", admin, """
                {"title":"짧은 안건","options":["찬성"]}
                """).andExpect(jsonPath("$.code").value("VOTE_OPTIONS_REQUIRED"));

        long voteId = createdId(postJson("/api/admin/votes", admin, """
                {"title":"회비 인상안","description":"월 회비를 올립니다","options":["찬성","반대"]}
                """));

        MockHttpSession voter1 = member("voter1");
        getJson("/api/votes", voter1).andExpect(jsonPath("$.content[0].resultCounts").doesNotExist());

        postJson("/api/votes/" + voteId + "/ballots", voter1, "{\"optionIndex\":5}")
                .andExpect(jsonPath("$.code").value("INVALID_VOTE_OPTION"));

        postJson("/api/votes/" + voteId + "/ballots", voter1, "{\"optionIndex\":0}")
                .andExpect(jsonPath("$.myOptionIndex").value(0))
                .andExpect(jsonPath("$.resultCounts").doesNotExist());

        postJson("/api/votes/" + voteId + "/ballots", voter1, "{\"optionIndex\":1}")
                .andExpect(jsonPath("$.code").value("ALREADY_VOTED"));

        MockHttpSession voter2 = member("voter2");
        postJson("/api/votes/" + voteId + "/ballots", voter2, "{\"optionIndex\":1}")
                .andExpect(jsonPath("$.myOptionIndex").value(1));

        JsonNode admin1 = body(getJson("/api/admin/votes", admin));
        assertThat(admin1.get("content").get(0).get("totalBallots").asInt()).isEqualTo(2);

        deleteJson("/api/admin/votes/" + voteId, admin, null).andExpect(jsonPath("$.code").value("VOTE_HAS_BALLOTS"));

        postJson("/api/admin/votes/" + voteId + "/close", admin, "").andExpect(jsonPath("$.open").value(false));
        postJson("/api/votes/" + voteId + "/ballots", voter1, "{\"optionIndex\":0}")
                .andExpect(jsonPath("$.code").value("VOTE_ALREADY_CLOSED"));

        getJson("/api/votes", voter1)
                .andExpect(jsonPath("$.content[0].resultCounts['0']").value(1))
                .andExpect(jsonPath("$.content[0].resultCounts['1']").value(1))
                .andExpect(jsonPath("$.content[0].totalBallots").value(2));
    }

    @Test
    void voteCanBeRestrictedToAMinimumGrade() throws Exception {
        MockHttpSession admin = admin();
        long vip = body(postJson("/api/admin/grades", admin, "{\"name\":\"정회원\",\"rank\":5}")).get("id").asLong();
        long voteId = createdId(postJson("/api/admin/votes", admin, """
                {"title":"정회원 전용 안건","options":["찬성","반대"],"accessGradeId":%d}
                """.formatted(vip)));

        MockHttpSession member = member("restricted");
        postJson("/api/votes/" + voteId + "/ballots", member, "{\"optionIndex\":0}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("GRADE_REQUIRED"));

        long memberId = body(getJson("/api/members/me", member)).get("id").asLong();
        putJson("/api/admin/members/" + memberId + "/grade", admin, """
                {"gradeId":%d,"expiresAt":"%s"}
                """.formatted(vip, LocalDateTime.now().plusDays(3).withNano(0))).andExpect(status().isNoContent());

        postJson("/api/votes/" + voteId + "/ballots", member, "{\"optionIndex\":0}")
                .andExpect(jsonPath("$.myOptionIndex").value(0));
    }
}

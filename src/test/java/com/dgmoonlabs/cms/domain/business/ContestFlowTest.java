package com.dgmoonlabs.cms.domain.business;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 공모전(작품 제출·심사위원 채점): 심사위원이 접수마다 점수를 매기고, 평균 점수가 높은 순으로 당첨자를 정한다. */
class ContestFlowTest extends ApiTestSupport {
    @Test
    void judgesScoreSubmissionsAndTopScorerWins() throws Exception {
        MockHttpSession admin = admin();
        MockHttpSession judge = member("judge1");

        postJson("/api/admin/applications", admin, """
                {"title":"심사위원 없는 공모전","isEnabled":true,"selectionMethod":"CONTEST","winnerCount":1,"fields":[
                  {"key":"title","label":"작품명","type":"TEXT","required":true}]}
                """).andExpect(jsonPath("$.code").value("JUDGES_REQUIRED"));

        postJson("/api/admin/applications", admin, """
                {"title":"없는 심사위원","isEnabled":true,"selectionMethod":"CONTEST","winnerCount":1,
                 "judgeUsernames":["nobody"],"fields":[{"key":"title","label":"작품명","type":"TEXT","required":true}]}
                """).andExpect(jsonPath("$.code").value("JUDGE_NOT_FOUND"));

        long formId = createdId(postJson("/api/admin/applications", admin, """
                {"title":"사진 공모전","isEnabled":true,"selectionMethod":"CONTEST","winnerCount":1,
                 "judgeUsernames":["judge1"],"fields":[{"key":"title","label":"작품명","type":"TEXT","required":true}]}
                """));

        MockHttpSession entrant1 = member("entrant1");
        MockHttpSession entrant2 = member("entrant2");
        long submission1 = body(postJson("/api/applications/%d/submissions".formatted(formId), entrant1, """
                {"answers":{"title":"작품 1"}}
                """)).get("id").asLong();
        long submission2 = body(postJson("/api/applications/%d/submissions".formatted(formId), entrant2, """
                {"answers":{"title":"작품 2"}}
                """)).get("id").asLong();

        postJson("/api/applications/submissions/" + submission1 + "/score", entrant1, """
                {"score":100}
                """).andExpect(jsonPath("$.code").value("NOT_JUDGE"));

        postJson("/api/applications/submissions/" + submission1 + "/score", judge, """
                {"score":90,"comment":"구도가 좋음"}
                """).andExpect(status().isOk());
        postJson("/api/applications/submissions/" + submission2 + "/score", judge, """
                {"score":70}
                """).andExpect(status().isOk());
        postJson("/api/applications/submissions/" + submission1 + "/score", judge, """
                {"score":95,"comment":"재심사"}
                """).andExpect(status().isOk());

        getJson("/api/applications/%d/judging".formatted(formId), judge)
                .andExpect(jsonPath("$.content[0].myScore").value(70))
                .andExpect(jsonPath("$.content[1].myScore").value(95));

        postJson("/api/admin/applications/%d/finalize-contest".formatted(formId), admin, "{}")
                .andExpect(jsonPath("$.candidates").value(2))
                .andExpect(jsonPath("$.winners").value(1));

        getJson("/api/admin/applications/%d/submissions".formatted(formId), admin)
                .andExpect(jsonPath("$.content[0].status").value("REJECTED"))
                .andExpect(jsonPath("$.content[0].averageScore").value(70))
                .andExpect(jsonPath("$.content[1].status").value("ACCEPTED"))
                .andExpect(jsonPath("$.content[1].averageScore").value(95));

        getJson("/api/admin/applications/submissions/" + submission1 + "/scores", admin)
                .andExpect(jsonPath("$[0].judgeUsername").value("judge1"))
                .andExpect(jsonPath("$[0].score").value(95));

        postJson("/api/admin/applications/%d/finalize-contest".formatted(formId), admin, "{}")
                .andExpect(jsonPath("$.code").value("ALREADY_FINALIZED"));
    }
}

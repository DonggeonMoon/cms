package com.dgmoonlabs.cms.domain.application;

import com.dgmoonlabs.cms.domain.admin.logging.repository.WatchdogLogRepository;
import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicationReportApiTest extends ApiTestSupport {
    private static final String FORM = """
            {"title":"만족도 조사","isEnabled":true,"allowMultiple":true,"fields":[
              {"key":"name","label":"소감","type":"TEXT","required":false},
              {"key":"score","label":"점수","type":"NUMBER","required":true},
              {"key":"route","label":"알게 된 경로","type":"RADIO","required":true,"options":["누리집","지인"]},
              {"key":"topics","label":"관심 분야","type":"CHECKBOX","required":false,"options":["교육","행사"]}]}
            """;

    @Autowired
    private WatchdogLogRepository watchdogLogRepository;

    @Test
    void adminsSeeRatiosAndAveragesAndDownloadCsv() throws Exception {
        MockHttpSession admin = admin();
        long formId = createdId(postJson("/api/admin/applications", admin, FORM));
        MockHttpSession member = member("surveyed");
        String url = "/api/applications/%d/submissions".formatted(formId);
        postJson(url, member, """
                {"answers":{"name":"=HYPERLINK(\\"x\\")","score":5,"route":"누리집","topics":["교육","행사"]}}""").andExpect(status().isCreated());
        postJson(url, member, """
                {"answers":{"name":"좋아요, 또 올게요","score":4,"route":"누리집","topics":["교육"]}}""").andExpect(status().isCreated());
        long withdrawn = body(postJson(url, member, """
                {"answers":{"score":1,"route":"지인"}}""")).get("id").asLong();
        postJson("/api/applications/submissions/%d/withdraw".formatted(withdrawn), member, "").andExpect(status().isOk());

        getJson("/api/admin/applications/%d/summary".formatted(formId), admin)
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.fields[?(@.key == 'score')].average").value(4.5))
                .andExpect(jsonPath("$.fields[?(@.key == 'score')].maximum").value(5))
                .andExpect(jsonPath("$.fields[?(@.key == 'route')].options[0].ratio").value(100.0))
                .andExpect(jsonPath("$.fields[?(@.key == 'route')].options[1].count").value(0))
                .andExpect(jsonPath("$.fields[?(@.key == 'topics')].options[1].ratio").value(50.0));
        getJson("/api/admin/applications/%d/summary?status=WITHDRAWN".formatted(formId), admin)
                .andExpect(jsonPath("$.total").value(1));

        long logsBefore = watchdogLogRepository.count();
        String csv = new String(mockMvc.perform(get("/api/admin/applications/%d/submissions.csv".formatted(formId)).session(admin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        assertThat(csv).startsWith("﻿신청 번호,아이디,이름,상태,신청 일시,소감,점수,알게 된 경로,관심 분야,결과 안내,관리자 메모\r\n");
        assertThat(csv).contains("\"'=HYPERLINK(\"\"x\"\")\"", "\"좋아요, 또 올게요\"", "\"교육, 행사\"", "surveyed님");
        assertThat(csv.lines()).hasSize(3);
        assertThat(watchdogLogRepository.count()).isEqualTo(logsBefore + 1);

        getJson("/api/admin/applications/%d/summary".formatted(formId), member).andExpect(status().is4xxClientError());
    }
}

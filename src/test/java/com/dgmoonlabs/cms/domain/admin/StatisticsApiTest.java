package com.dgmoonlabs.cms.domain.admin;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StatisticsApiTest extends ApiTestSupport {
    private static final String CHROME_MAC = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36";

    private void visit(final String path) throws Exception {
        mockMvc.perform(post("/api/statistics/visits").with(csrf()).header("User-Agent", CHROME_MAC)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"path\":\"%s\"}".formatted(path)))
                .andExpect(status().isNoContent());
    }

    @Test
    void visitsAreAggregatedForTheDashboard() throws Exception {
        visit("/boards/1?page=2");
        visit("/boards/1");
        visit("/");

        MockHttpSession admin = admin();
        LocalDate today = LocalDate.now();
        getJson("/api/admin/statistics/daily?from=%s&to=%s".formatted(today.minusDays(2), today), admin)
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].visits").value(0))
                .andExpect(jsonPath("$[2].visits").value(3));
        getJson("/api/admin/statistics/breakdown?dimension=URL&from=%s&to=%s".formatted(today, today), admin)
                .andExpect(jsonPath("$[0].label").value("/boards/1"))
                .andExpect(jsonPath("$[0].visits").value(2));
        getJson("/api/admin/statistics/breakdown?dimension=BROWSER&from=%s&to=%s".formatted(today, today), admin)
                .andExpect(jsonPath("$[0].label").value("Chrome"));
        getJson("/api/admin/dashboard", admin)
                .andExpect(jsonPath("$.todayVisits").value(3))
                .andExpect(jsonPath("$.last7DaysVisits.length()").value(7));

        getJson("/api/admin/statistics/daily?from=%s&to=%s".formatted(today, today.minusDays(1)), admin)
                .andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
    }
}

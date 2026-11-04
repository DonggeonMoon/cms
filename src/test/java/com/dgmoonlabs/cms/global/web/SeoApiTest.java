package com.dgmoonlabs.cms.global.web;

import com.dgmoonlabs.cms.domain.admin.site.dto.SiteResponse;
import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SeoApiTest extends ApiTestSupport {
    @Test
    void siteSeoSettingsReachTheServedPageRobotsAndSitemap() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/sites", admin, """
                {"name":"동네 카페","isDefault":true,"naverVerification":"bad code!"}
                """).andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        long siteId = createdId(postJson("/api/admin/sites", admin, """
                {"name":"동네 카페","isDefault":true,"seoTitle":"동네 카페 | 핸드드립","metaDescription":"매일 볶는 원두 <신선>",
                 "metaKeywords":"카페, 원두","googleVerification":"abc_123"}
                """));
        getJson("/api/site", null)
                .andExpect(jsonPath("$.darkModeEnabled").value(true))
                .andExpect(jsonPath("$.seoTitle").value("동네 카페 | 핸드드립"))
                .andExpect(jsonPath("$.searchIndexing").value(true));
        long contentId = createdId(postJson("/api/admin/contents", admin,
                "{\"title\":\"소개\",\"contentHtml\":\"<p>안녕</p>\",\"status\":\"PUBLISHED\"}"));

        String page = mockMvc.perform(get("/products/1")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(page).contains("<meta charset=\"UTF-8\"/>", "<title>동네 카페 | 핸드드립</title>",
                "<meta name=\"description\" content=\"매일 볶는 원두 &lt;신선&gt;\"/>",
                "<meta property=\"og:title\" content=\"동네 카페 | 핸드드립\"/>",
                "<meta name=\"google-site-verification\" content=\"abc_123\"/>", "<div id=\"root\">spa</div>");
        mockMvc.perform(get("/robots.txt")).andExpect(content().string(containsString("Sitemap: http://localhost/sitemap.xml")));
        mockMvc.perform(get("/sitemap.xml"))
                .andExpect(content().string(containsString("<loc>http://localhost/contents/" + contentId + "</loc>")));

        putJson("/api/admin/sites/" + siteId, admin, "{\"name\":\"동네 카페\",\"isDefault\":true,\"searchIndexing\":false,\"darkModeEnabled\":false}")
                .andExpect(status().isNoContent());
        getJson("/api/site", null).andExpect(jsonPath("$.darkModeEnabled").value(false));
        mockMvc.perform(get("/robots.txt")).andExpect(content().string("User-agent: *\nDisallow: /\n"));
        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("<meta name=\"robots\" content=\"noindex, nofollow\"/>")))
                .andExpect(content().string(not(containsString("og:image"))));
    }

    @Test
    void replacesTheTitleAndFaviconOfARealIndexPage() {
        SiteResponse site = new SiteResponse(1L, "카페", null, null, null, null, null, true, null, Map.of(), true, 3L, "/api/files/3/view",
                null, "설명", null, 4L, "/api/files/4/view", true, null, null, null);
        String html = SeoIndexRenderer.inject("<html><head><link rel=\"icon\" href=\"/favicon.ico\"/><title>cms</title></head><body></body></html>",
                site, "https://cafe.example");
        assertThat(html).contains("<title>카페</title>", "<link rel=\"icon\" href=\"/api/files/3/view\"/>",
                "<meta property=\"og:image\" content=\"https://cafe.example/api/files/4/view\"/>").doesNotContain("favicon.ico");
    }
}

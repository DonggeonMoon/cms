package com.dgmoonlabs.cms;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SpaFallbackTest extends ApiTestSupport {
    @Test
    void clientRoutesServeTheReactAppButApiAndFilePathsDoNot() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        for (String path : new String[]{"/boards/1", "/admin/members", "/products/3/reviews"}) {
            mockMvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("<div id=\"root\">")));
        }
        mockMvc.perform(get("/api/does-not-exist")).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").exists());
        mockMvc.perform(get("/assets/missing.js")).andExpect(status().isNotFound());
    }
}

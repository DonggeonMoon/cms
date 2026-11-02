package com.dgmoonlabs.cms;

import com.dgmoonlabs.cms.domain.home.controller.HomeController;
import com.dgmoonlabs.cms.global.config.I18nConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.containsString;

@WebMvcTest(controllers = HomeController.class, properties = "locale.parameterName=lang")
@Import(I18nConfig.class)
class CmsApplicationTests {
    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser
    void homeRendersTemplate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("home"))
                .andExpect(MockMvcResultMatchers.content().string(containsString("I'm header.")));
    }
}

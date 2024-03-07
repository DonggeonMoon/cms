package com.dgmoonlabs.cms;

import com.dgmoonlabs.cms.domain.home.controller.HomeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(HomeController.class)
class CmsApplicationTests {
    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser
    void xssFilterTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .header("header", "scripthaha")
                        .with(csrf())
                )
                .andExpect(MockMvcResultMatchers.header().string("header", "haha"));

    }

}

package com.dgmoonlabs.cms.global.filter.xss;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class XssRequestWrapperTest {
    @Test
    void sanitizesHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("header", "scripthaha");

        assertThat(new XssRequestWrapper(request).getHeader("header")).isEqualTo("haha");
    }

    @Test
    void missingParameterStaysMissing() {
        assertThat(new XssRequestWrapper(new MockHttpServletRequest()).getParameterValues("absent")).isNull();
    }
}

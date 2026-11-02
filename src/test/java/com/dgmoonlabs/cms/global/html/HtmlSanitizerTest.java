package com.dgmoonlabs.cms.global.html;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlSanitizerTest {
    private final HtmlSanitizer sanitizer = new HtmlSanitizer();

    @Test
    void keepsLandingPageLayoutMarkupButNotScripts() {
        String html = sanitizer.sanitize("""
                <section class="hero"><h1 class="title">중고천국</h1><p class="hero-description">설명</p>
                <div data-block="new-products"></div><div data-block="x&quot; onclick=&quot;y"></div>
                <a href="/products" class="chip" onclick="alert(1)">상품</a><script>alert(1)</script></section>
                """);

        assertThat(html)
                .contains("<section class=\"hero\">", "<h1 class=\"title\">", "<p class=\"hero-description\">")
                .contains("<div data-block=\"new-products\"></div>", "class=\"chip\"")
                .doesNotContain("<script", "onclick", "x&#34;");
    }
}

package com.dgmoonlabs.cms.global.html;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class HtmlSanitizer {
    private static final Pattern CLASS_NAMES = Pattern.compile("[A-Za-z0-9_\\- ]{1,200}");
    private static final Pattern BLOCK_NAME = Pattern.compile("[a-z-]{1,50}");

    private static final PolicyFactory RICH_TEXT = Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.LINKS)
            .and(Sanitizers.IMAGES)
            .and(Sanitizers.TABLES)
            .and(Sanitizers.STYLES)
            .and(new HtmlPolicyBuilder().allowElements("hr", "pre", "code", "span", "figure", "figcaption").toFactory())
            .and(new HtmlPolicyBuilder()
                    .allowElements("section", "header", "article", "aside", "nav", "div")
                    .allowAttributes("class").matching(CLASS_NAMES).globally()
                    .allowAttributes("data-block").matching(BLOCK_NAME).onElements("div")
                    .toFactory());

    public String sanitize(final String html) {
        return html == null ? "" : RICH_TEXT.sanitize(html);
    }

    public String toPlainText(final String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&amp;", "&")
                .replaceAll("\\s+", " ")
                .trim();
    }
}

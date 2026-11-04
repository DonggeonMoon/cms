package com.dgmoonlabs.cms.global.web;

import com.dgmoonlabs.cms.domain.admin.site.dto.SiteResponse;
import com.dgmoonlabs.cms.domain.admin.site.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;

/**
 * 링크 미리보기(카카오톡·페이스북 등)는 자바스크립트를 실행하지 않으므로, index.html 을 내려줄 때
 * 기본 사이트의 제목·파비콘·메타·OG 태그를 서버에서 미리 채워 넣는다.
 */
@Component
@RequiredArgsConstructor
public class SeoIndexRenderer {
    private static final String INDEX = "/static/index.html";

    private final SiteService siteService;
    private volatile String template;

    public Optional<String> render(final String baseUrl) {
        return loadTemplate().map(html -> inject(html, siteService.getDefaultSite(), baseUrl));
    }

    static String inject(final String html, final SiteResponse site, final String baseUrl) {
        String title = escape(site.titleOrName());
        StringBuilder head = new StringBuilder();
        meta(head, "name", "description", site.metaDescription());
        meta(head, "name", "keywords", site.metaKeywords());
        if (!site.searchIndexing()) {
            meta(head, "name", "robots", "noindex, nofollow");
        }
        meta(head, "name", "google-site-verification", site.googleVerification());
        meta(head, "name", "naver-site-verification", site.naverVerification());
        meta(head, "property", "og:type", "website");
        meta(head, "property", "og:site_name", site.name());
        meta(head, "property", "og:title", site.titleOrName());
        meta(head, "property", "og:description", site.metaDescription());
        meta(head, "property", "og:url", baseUrl + "/");
        if (site.ogImageUrl() != null) {
            meta(head, "property", "og:image", baseUrl + site.ogImageUrl());
            meta(head, "name", "twitter:card", "summary_large_image");
        }
        String result = html;
        if (!result.toLowerCase().contains("charset")) {
            head.insert(0, "    <meta charset=\"UTF-8\"/>\n");
        }
        if (result.contains("<title>")) {
            result = result.replaceFirst("<title>[^<]*</title>", Matcher.quoteReplacement("<title>" + title + "</title>"));
        } else {
            head.insert(0, "    <title>" + title + "</title>\n");
        }
        if (site.faviconUrl() != null) {
            String icon = "<link rel=\"icon\" href=\"" + escape(site.faviconUrl()) + "\"/>";
            if (result.contains("<link rel=\"icon\"")) {
                result = result.replaceFirst("<link rel=\"icon\"[^>]*>", Matcher.quoteReplacement(icon));
            } else {
                head.append("    ").append(icon).append("\n");
            }
        }
        if (result.contains("</head>")) {
            return result.replace("</head>", head + "</head>");
        }
        int body = result.indexOf("<body");
        return body < 0 ? "<head>\n" + head + "</head>" + result : result.substring(0, body) + "<head>\n" + head + "</head>" + result.substring(body);
    }

    private static void meta(final StringBuilder head, final String attribute, final String key, final String value) {
        if (value != null && !value.isBlank()) {
            head.append("    <meta ").append(attribute).append("=\"").append(key).append("\" content=\"").append(escape(value)).append("\"/>\n");
        }
    }

    private static String escape(final String value) {
        return HtmlUtils.htmlEscape(value, StandardCharsets.UTF_8.name());
    }

    private Optional<String> loadTemplate() {
        if (template == null) {
            ClassPathResource resource = new ClassPathResource(INDEX);
            if (!resource.exists()) {
                return Optional.empty();
            }
            try (InputStream in = resource.getInputStream()) {
                template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return Optional.empty();
            }
        }
        return Optional.of(template);
    }
}

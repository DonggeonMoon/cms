package com.dgmoonlabs.cms.global.web;

import com.dgmoonlabs.cms.domain.admin.site.dto.SiteResponse;
import com.dgmoonlabs.cms.domain.admin.site.service.SiteService;
import com.dgmoonlabs.cms.global.web.SitemapService.Entry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.HtmlUtils;

import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class SeoController {
    private final SeoIndexRenderer renderer;
    private final SiteService siteService;
    private final SitemapService sitemapService;

    @GetMapping(value = {"/", "/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> index() {
        return renderer.render(baseUrl())
                .map(html -> ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(html))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> robots() {
        SiteResponse site = siteService.getDefaultSite();
        String body = site.searchIndexing()
                ? "User-agent: *\nDisallow: /admin\nDisallow: /api/\nDisallow: /mypage\nAllow: /\n\nSitemap: " + baseUrl() + "/sitemap.xml\n"
                : "User-agent: *\nDisallow: /\n";
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS)).body(body);
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        String base = baseUrl();
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (Entry entry : sitemapService.entries()) {
            xml.append("  <url><loc>").append(HtmlUtils.htmlEscape(base + entry.path())).append("</loc>");
            if (entry.lastModified() != null) {
                xml.append("<lastmod>").append(entry.lastModified().toLocalDate()).append("</lastmod>");
            }
            xml.append("</url>\n");
        }
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS)).body(xml.append("</urlset>\n").toString());
    }

    private static String baseUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }
}

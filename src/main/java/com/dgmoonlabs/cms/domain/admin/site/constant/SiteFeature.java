package com.dgmoonlabs.cms.domain.admin.site.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum SiteFeature {
    CONTENT("소개 페이지(컨텐츠)", List.of("/api/contents"), List.of("/api/admin/contents")),
    BOARD("게시판", List.of("/api/boards"), List.of("/api/admin/boards")),
    ARCHIVE("아카이브", List.of("/api/archives"), List.of("/api/admin/archives")),
    QNA("질문답변", List.of("/api/questions"), List.of("/api/admin/questions")),
    MAP("지도", List.of("/api/map"), List.of("/api/admin/map")),
    PRODUCT("상품", List.of("/api/products", "/api/orders"),
            List.of("/api/admin/products", "/api/admin/discounts", "/api/admin/orders", "/api/admin/payments", "/api/admin/reviews")),
    PROGRAM("프로그램", List.of("/api/programs", "/api/reservations"),
            List.of("/api/admin/programs", "/api/admin/reservations", "/api/admin/payments", "/api/admin/reviews")),
    APPLICATION("신청·지원", List.of("/api/applications"), List.of("/api/admin/applications")),
    MEDIA("스트리밍", List.of("/api/media"), List.of("/api/admin/media")),
    REPORT("신고", List.of("/api/reports"), List.of("/api/admin/reports")),
    SATISFACTION("만족도 조사", List.of("/api/satisfactions"), List.of("/api/admin/satisfactions")),
    CHATBOT("챗봇", List.of("/api/chatbot"), List.of("/api/admin/chatbot"));

    private final String label;
    private final List<String> pathPrefixes;
    private final List<String> adminPathPrefixes;

    public static List<SiteFeature> ofPath(final String path) {
        return Arrays.stream(values())
                .filter(feature -> matches(feature.pathPrefixes, path) || matches(feature.adminPathPrefixes, path))
                .toList();
    }

    private static boolean matches(final List<String> prefixes, final String path) {
        return prefixes.stream().anyMatch(prefix -> path.equals(prefix) || path.startsWith(prefix + "/"));
    }
}

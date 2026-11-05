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
    PRODUCT("상품", List.of("/api/products", "/api/orders", "/api/store-setting", "/api/wishlist", "/api/passes", "/api/progress", "/api/messages", "/api/wallet", "/api/license-keys"),
            List.of("/api/admin/products", "/api/admin/store-setting", "/api/admin/store-orders", "/api/admin/discounts", "/api/admin/orders", "/api/admin/payments", "/api/admin/reviews", "/api/admin/passes", "/api/admin/progress", "/api/admin/messages", "/api/admin/license-keys")),
    PROGRAM("예약", List.of("/api/programs", "/api/reservations", "/api/customer-records", "/api/class-attendance"),
            List.of("/api/admin/programs", "/api/admin/reservations", "/api/admin/payments", "/api/admin/reviews", "/api/admin/customer-records",
                    "/api/admin/class-attendance")),
    APPLICATION("신청·지원", List.of("/api/applications", "/api/repairs", "/api/progress", "/api/messages", "/api/votes"),
            List.of("/api/admin/applications", "/api/admin/repairs", "/api/admin/progress", "/api/admin/messages", "/api/admin/votes")),
    MEDIA("스트리밍", List.of("/api/media", "/api/courses"), List.of("/api/admin/media", "/api/admin/courses")),
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

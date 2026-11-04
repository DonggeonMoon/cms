package com.dgmoonlabs.cms.domain.admin.account.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/** 관리자 등급으로 나눠 줄 수 있는 관리 영역. 관리자 화면의 메뉴 묶음과 같고, 관리자 API 경로 첫 단어로 구분한다. */
@Getter
public enum AdminArea {
    STATUS("현황", "dashboard", "statistics"),
    SITE("사이트", "sites", "menus", "banners", "configs"),
    CONTENT("컨텐츠", "contents", "boards", "archives", "questions", "map"),
    BUSINESS("비즈니스", "products", "product-categories", "discounts", "orders", "shipping-policy", "store-setting", "store-orders", "subscriptions",
            "programs", "reservations", "applications", "media", "courses", "payments", "reviews", "statuses"),
    PUBLIC("공공", "reports", "satisfactions"),
    MEMBER("회원", "members", "grades", "points", "coupons", "policies"),
    COMMON("공통 설정", "chatbot", "regexes", "regex-bindings", "codes", "i18n", "files", "audit-logs"),
    SECURITY("보안", "api-keys", "password-hash", "crypto-key", "social-logins");

    /** 관리자 관리는 등급으로 나눠 줄 수 없고 최고 관리자만 쓴다. */
    public static final Set<String> SUPER_ONLY = Set.of("admins", "admin-grades");
    public static final Set<String> ALWAYS = Set.of("reauthenticate", "me");

    private final String label;
    private final Set<String> segments;

    AdminArea(final String label, final String... segments) {
        this.label = label;
        this.segments = Set.of(segments);
    }

    public static Optional<AdminArea> ofSegment(final String segment) {
        return Arrays.stream(values()).filter(area -> area.segments.contains(segment)).findFirst();
    }
}

package com.dgmoonlabs.cms.domain.common.social.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialProvider {
    KAKAO("카카오", "https://kauth.kakao.com/oauth/authorize", "", "https://developers.kakao.com/console/app"),
    NAVER("네이버", "https://nid.naver.com/oauth2.0/authorize", "", "https://developers.naver.com/apps/#/list"),
    GOOGLE("구글", "https://accounts.google.com/o/oauth2/v2/auth", "openid email profile",
            "https://console.cloud.google.com/apis/credentials"),
    APPLE("애플", "https://appleid.apple.com/auth/authorize", "name email",
            "https://developer.apple.com/account/resources/identifiers/list/serviceId"),
    FACEBOOK("페이스북", "https://www.facebook.com/" + SocialProvider.FACEBOOK_GRAPH_VERSION + "/dialog/oauth", "email public_profile",
            "https://developers.facebook.com/apps/");

    // Graph API 버전은 약 2년 뒤 종료되므로 Meta 공지에 맞춰 올린다.
    public static final String FACEBOOK_GRAPH_VERSION = "v23.0";

    private final String label;
    private final String authorizationUri;
    private final String scope;
    private final String consoleUrl;

    public String path() {
        return name().toLowerCase();
    }

    public static SocialProvider fromPath(final String path) {
        for (SocialProvider provider : values()) {
            if (provider.path().equals(path)) {
                return provider;
            }
        }
        return null;
    }
}

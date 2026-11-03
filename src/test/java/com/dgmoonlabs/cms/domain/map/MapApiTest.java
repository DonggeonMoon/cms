package com.dgmoonlabs.cms.domain.map;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MapApiTest extends ApiTestSupport {
    private static final String HEAD_OFFICE = """
            "officeName":"본점","officeAddress":"서울 중구 세종대로 110","officeLatitude":37.5666103,
            "officeLongitude":126.9783882,"officePhoneNumber":"02-120"
            """;

    @Test
    void nothingIsShownUntilTabsAreSwitchedOn() throws Exception {
        getJson("/api/map", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("KAKAO"))
                .andExpect(jsonPath("$.directions").doesNotExist())
                .andExpect(jsonPath("$.branches").doesNotExist());
    }

    @Test
    void directionsNeedAnOfficeLocation() throws Exception {
        MockHttpSession admin = admin();
        putJson("/api/admin/map/settings", admin, """
                {"provider":"KAKAO","directionsEnabled":true,"officeName":"본점"}
                """).andExpect(jsonPath("$.code").value("MAP_LOCATION_REQUIRED"));

        postJson("/api/admin/reauthenticate", admin, """
                {"password":"adminpass1"}
                """).andExpect(status().isNoContent());
        long kakaoKey = createdId(postJson("/api/admin/api-keys", admin, """
                {"name":"카카오맵 JS 키","value":"kakao-key"}
                """));
        long naverKey = createdId(postJson("/api/admin/api-keys", admin, """
                {"name":"네이버 지도 Client ID","value":"naver-id"}
                """));
        putJson("/api/admin/map/settings", admin, """
                {"provider":"NAVER","kakaoApiKeyId":%d,"naverApiKeyId":%d,"directionsEnabled":true,%s}
                """.formatted(kakaoKey, naverKey, HEAD_OFFICE)).andExpect(status().isOk())
                .andExpect(jsonPath("$.naverApiKeyId").value(naverKey));

        getJson("/api/map", null)
                .andExpect(jsonPath("$.provider").value("NAVER"))
                .andExpect(jsonPath("$.apiKey").value("naver-id"))
                .andExpect(jsonPath("$.directions.address").value("서울 중구 세종대로 110"))
                .andExpect(jsonPath("$.directions.latitude").value(37.5666103))
                .andExpect(jsonPath("$.branches").doesNotExist());

        long googleKey = createdId(postJson("/api/admin/api-keys", admin, """
                {"name":"구글 지도 API 키","value":"google-key"}
                """));
        putJson("/api/admin/map/settings", admin, """
                {"provider":"GOOGLE","naverApiKeyId":%d,"googleApiKeyId":%d,"directionsEnabled":true,%s}
                """.formatted(naverKey, googleKey, HEAD_OFFICE)).andExpect(jsonPath("$.googleApiKeyId").value(googleKey));
        getJson("/api/map", null)
                .andExpect(jsonPath("$.provider").value("GOOGLE"))
                .andExpect(jsonPath("$.apiKey").value("google-key"));
    }

    @Test
    void branchesAreManagedOnlyWhileTheTabIsOn() throws Exception {
        MockHttpSession admin = admin();
        String branch = """
                {"name":"강남점","address":"서울 강남구 테헤란로 1","latitude":37.498,"longitude":127.027,"sequence":1,"isUsing":true}
                """;
        postJson("/api/admin/map/branches", admin, branch).andExpect(jsonPath("$.code").value("BRANCHES_DISABLED"));

        putJson("/api/admin/map/settings", admin, """
                {"provider":"KAKAO","branchesEnabled":true}
                """).andExpect(status().isOk());
        postJson("/api/admin/map/branches", admin, branch).andExpect(status().isCreated());
        postJson("/api/admin/map/branches", admin, """
                {"name":"숨긴 지점","address":"부산","latitude":35.1,"longitude":129.0,"isUsing":false}
                """).andExpect(status().isCreated());
        postJson("/api/admin/map/branches", admin, """
                {"name":"좌표 없음","address":"어딘가"}
                """).andExpect(status().isBadRequest());

        getJson("/api/map", null)
                .andExpect(jsonPath("$.branches.length()").value(1))
                .andExpect(jsonPath("$.branches[0].name").value("강남점"))
                .andExpect(jsonPath("$.directions").doesNotExist());
        getJson("/api/admin/map/branches", admin).andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void mapFeatureCanBeSwitchedOff() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/sites", admin, """
                {"name":"cms","isDefault":true,"disabledFeatures":["MAP"]}
                """).andExpect(status().isCreated());

        getJson("/api/map", null).andExpect(jsonPath("$.code").value("FEATURE_DISABLED"));
        getJson("/api/admin/map/settings", admin).andExpect(jsonPath("$.code").value("FEATURE_DISABLED"));
    }
}

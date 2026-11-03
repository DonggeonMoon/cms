package com.dgmoonlabs.cms.domain.admin;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminApiTest extends ApiTestSupport {
    @Test
    void onlyOneSiteIsDefault() throws Exception {
        MockHttpSession admin = admin();
        long first = createdId(postJson("/api/admin/sites", admin, """
                {"name":"메인","domain":"a.example.com","isDefault":true}
                """).andExpect(status().isCreated()));
        postJson("/api/admin/sites", admin, """
                {"name":"영문","domain":"en.example.com","isDefault":true}
                """).andExpect(status().isCreated());

        getJson("/api/site", null).andExpect(jsonPath("$.name").value("영문"));
        getJson("/api/admin/sites/" + first, admin).andExpect(jsonPath("$.isDefault").value(false));
    }

    @Test
    void siteFallsBackToCmsWithEveryFeatureOn() throws Exception {
        getJson("/api/site", null)
                .andExpect(jsonPath("$.name").value("cms"))
                .andExpect(jsonPath("$.features.PRODUCT").value(true));
    }

    @Test
    void disabledFeaturesAreClosedToVisitorsAndTheirAdminApiToo() throws Exception {
        MockHttpSession admin = admin();
        long siteId = createdId(postJson("/api/admin/sites", admin, """
                {"name":"우리 기관","isDefault":true,"disabledFeatures":["PRODUCT","QNA"]}
                """).andExpect(status().isCreated()));

        getJson("/api/site", null)
                .andExpect(jsonPath("$.name").value("우리 기관"))
                .andExpect(jsonPath("$.features.PRODUCT").value(false))
                .andExpect(jsonPath("$.features.PROGRAM").value(true));
        getJson("/api/products", null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FEATURE_DISABLED"));
        getJson("/api/questions", member("visitor")).andExpect(jsonPath("$.code").value("FEATURE_DISABLED"));
        getJson("/api/programs", null).andExpect(status().isOk());
        getJson("/api/products", admin).andExpect(status().isOk());

        long contentId = createdId(postJson("/api/admin/contents", admin, """
                {"title":"소개","contentHtml":"<p>기관 소개</p>","status":"PUBLISHED"}
                """).andExpect(status().isCreated()));
        putJson("/api/admin/sites/" + siteId, admin, """
                {"name":"우리 기관","isDefault":true,"disabledFeatures":["CONTENT"]}
                """).andExpect(status().isNoContent());
        getJson("/api/contents/" + contentId, null).andExpect(jsonPath("$.code").value("FEATURE_DISABLED"));
        getJson("/api/admin/contents/" + contentId, admin)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FEATURE_DISABLED"));
        getJson("/api/products", null).andExpect(status().isOk());
        getJson("/api/admin/sites/" + siteId, admin).andExpect(status().isOk());
    }

    @Test
    void sharedBusinessAdminApiClosesOnlyWhenEveryOwnerIsOff() throws Exception {
        MockHttpSession admin = admin();
        long siteId = createdId(postJson("/api/admin/sites", admin, """
                {"name":"cms","isDefault":true,"disabledFeatures":["PRODUCT"]}
                """).andExpect(status().isCreated()));

        getJson("/api/admin/discounts", admin).andExpect(jsonPath("$.code").value("FEATURE_DISABLED"));
        getJson("/api/admin/payments", admin).andExpect(status().isOk());

        putJson("/api/admin/sites/" + siteId, admin, """
                {"name":"cms","isDefault":true,"disabledFeatures":["PRODUCT","PROGRAM"]}
                """).andExpect(status().isNoContent());
        getJson("/api/admin/payments", admin).andExpect(jsonPath("$.code").value("FEATURE_DISABLED"));
        getJson("/api/admin/reservations", admin).andExpect(jsonPath("$.code").value("FEATURE_DISABLED"));
    }

    @Test
    void aContentCanServeAsTheHomePage() throws Exception {
        MockHttpSession admin = admin();
        long contentId = createdId(postJson("/api/admin/contents", admin, """
                {"title":"홈","contentHtml":"<section class=\\"hero\\"><div data-block=\\"new-products\\"></div></section>","status":"PUBLISHED"}
                """).andExpect(status().isCreated()));
        postJson("/api/admin/sites", admin, """
                {"name":"cms","isDefault":true,"homeContentId":%d}
                """.formatted(contentId)).andExpect(status().isCreated());

        getJson("/api/site", null).andExpect(jsonPath("$.homeContentId").value(contentId));
        getJson("/api/contents/" + contentId, null)
                .andExpect(jsonPath("$.contentHtml").value("<section class=\"hero\"><div data-block=\"new-products\"></div></section>"));
        postJson("/api/admin/sites", admin, """
                {"name":"없는 홈","homeContentId":9999}
                """).andExpect(status().isNotFound());
    }

    @Test
    void menusCannotPointAtSwitchedOffFeatures() throws Exception {
        MockHttpSession admin = admin();
        long siteId = createdId(postJson("/api/admin/sites", admin, """
                {"name":"cms","isDefault":true,"disabledFeatures":["BOARD"]}
                """).andExpect(status().isCreated()));

        postJson("/api/admin/menus", admin, """
                {"name":"자유게시판","type":"BOARD","targetId":1}
                """).andExpect(jsonPath("$.code").value("MENU_FEATURE_DISABLED"));
        long linkMenu = createdId(postJson("/api/admin/menus", admin, """
                {"name":"외부","type":"LINK","link":"https://example.com"}
                """).andExpect(status().isCreated()));
        putJson("/api/admin/menus/" + linkMenu, admin, """
                {"name":"외부","type":"BOARD","targetId":1}
                """).andExpect(jsonPath("$.code").value("MENU_FEATURE_DISABLED"));

        putJson("/api/admin/sites/" + siteId, admin, """
                {"name":"cms","isDefault":true,"disabledFeatures":[]}
                """).andExpect(status().isNoContent());
        postJson("/api/admin/menus", admin, """
                {"name":"자유게시판","type":"BOARD","targetId":1}
                """).andExpect(status().isCreated());
    }

    @Test
    void configsExposeOnlyVisibleKeys() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/configs", admin, """
                {"key":"site.title","value":"DG CMS","type":"TEXT"}
                """).andExpect(status().isCreated());
        postJson("/api/admin/configs", admin, """
                {"key":"secret.token","value":"x","type":"TEXT","isHidden":true}
                """).andExpect(status().isCreated());
        postJson("/api/admin/configs", admin, """
                {"key":"site.title","value":"dup","type":"TEXT"}
                """).andExpect(jsonPath("$.code").value("DUPLICATE_CONFIG_KEY"));

        getJson("/api/configs", null)
                .andExpect(jsonPath("$['site.title']").value("DG CMS"))
                .andExpect(jsonPath("$['secret.token']").doesNotExist());
    }

    @Test
    void menusFormATree() throws Exception {
        MockHttpSession admin = admin();
        long root = createdId(postJson("/api/admin/menus", admin, """
                {"name":"소개","type":"CONTENT","targetId":1,"sequence":1}
                """).andExpect(status().isCreated()));
        postJson("/api/admin/menus", admin, """
                {"parentId":%d,"name":"오시는 길","type":"LINK","link":"https://map.example.com","sequence":1}
                """.formatted(root)).andExpect(status().isCreated());
        postJson("/api/admin/menus", admin, """
                {"name":"잘못된 링크","type":"LINK"}
                """).andExpect(status().isBadRequest());

        getJson("/api/menus", null)
                .andExpect(jsonPath("$[?(@.id == %d)].children[0].name".formatted(root)).value(hasItem("오시는 길")))
                .andExpect(jsonPath("$[?(@.id == %d)].children[0].depth".formatted(root)).value(hasItem(1)));
        deleteJson("/api/admin/menus/" + root, admin, null).andExpect(jsonPath("$.code").value("MENU_HAS_CHILDREN"));
    }

    @Test
    void siblingMenusCanBeReorderedAtOnce() throws Exception {
        MockHttpSession admin = admin();
        long parent = createdId(postJson("/api/admin/menus", admin, """
                {"name":"부모","type":"LINK","link":"/p","sequence":1}
                """));
        long first = createdId(postJson("/api/admin/menus", admin, """
                {"parentId":%d,"name":"첫째","type":"LINK","link":"/1","sequence":1}
                """.formatted(parent)));
        long second = createdId(postJson("/api/admin/menus", admin, """
                {"parentId":%d,"name":"둘째","type":"LINK","link":"/2","sequence":1}
                """.formatted(parent)));

        putJson("/api/admin/menus/order", admin, """
                {"parentId":%d,"ids":[%d,%d]}
                """.formatted(parent, second, first)).andExpect(status().isNoContent());
        getJson("/api/admin/menus/" + second, admin).andExpect(jsonPath("$.sequence").value(1));
        getJson("/api/admin/menus/" + first, admin).andExpect(jsonPath("$.sequence").value(2));

        putJson("/api/admin/menus/order", admin, """
                {"parentId":%d,"ids":[%d]}
                """.formatted(parent, first)).andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        putJson("/api/admin/menus/order", admin, """
                {"parentId":%d,"ids":[%d,%d]}
                """.formatted(parent, first, first)).andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void featureMenusAreAddedByAdminsAndFollowSiteFeatures() throws Exception {
        MockHttpSession admin = admin();
        getJson("/api/admin/menus", admin).andExpect(jsonPath("$[?(@.type == 'FEATURE')]").isEmpty());

        long productMenu = createdId(postJson("/api/admin/menus", admin, """
                {"name":"상품","type":"FEATURE","feature":"PRODUCT","sequence":1}
                """).andExpect(status().isCreated()));
        long qnaMenu = createdId(postJson("/api/admin/menus", admin, """
                {"name":"질문답변","type":"FEATURE","feature":"QNA","sequence":2}
                """).andExpect(status().isCreated()));
        postJson("/api/admin/menus", admin, """
                {"name":"기능 없음","type":"FEATURE"}
                """).andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        postJson("/api/admin/menus", admin, """
                {"name":"만족도","type":"FEATURE","feature":"SATISFACTION"}
                """).andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        putJson("/api/admin/menus/" + productMenu, admin, """
                {"name":"중고 상품","type":"FEATURE","feature":"PRODUCT","sequence":1,"isUsing":true}
                """).andExpect(status().isNoContent());
        putJson("/api/admin/menus/" + qnaMenu, admin, """
                {"name":"질문답변","type":"FEATURE","feature":"QNA","sequence":2,"isUsing":false}
                """).andExpect(status().isNoContent());
        getJson("/api/menus", null)
                .andExpect(jsonPath("$[?(@.feature == 'PRODUCT')].name").value(hasItem("중고 상품")))
                .andExpect(jsonPath("$[?(@.feature == 'QNA')]").isEmpty());

        postJson("/api/admin/menus", admin, """
                {"parentId":%d,"name":"하위","type":"LINK","link":"/x"}
                """.formatted(productMenu)).andExpect(jsonPath("$.code").value("MENU_BUILT_IN"));
        deleteJson("/api/admin/menus/" + productMenu, admin, null).andExpect(status().isNoContent());

        postJson("/api/admin/sites", admin, """
                {"name":"cms","isDefault":true,"disabledFeatures":["MAP"]}
                """).andExpect(status().isCreated());
        postJson("/api/admin/menus", admin, """
                {"name":"지도","type":"FEATURE","feature":"MAP"}
                """).andExpect(jsonPath("$.code").value("MENU_FEATURE_DISABLED"));
    }

    @Test
    void guestPostingFollowsFeatureSettings() throws Exception {
        MockHttpSession admin = admin();
        String question = """
                {"title":"문의","contentHtml":"<p>?</p>","authorName":"손님","password":"pw1234"%s}
                """;
        String report = """
                {"title":"신고","contentHtml":"<p>!</p>","reporterName":"손님","password":"pw1234"%s}
                """;
        getJson("/api/questions/guest-posting", null)
                .andExpect(jsonPath("$.allowed").value(true))
                .andExpect(jsonPath("$.collectPhone").value(false))
                .andExpect(jsonPath("$.collectEmail").value(true))
                .andExpect(jsonPath("$.contactChosen").doesNotExist());
        postJson("/api/questions", null, question.formatted(""))
                .andExpect(jsonPath("$.code").value("GUEST_CONTACT_REQUIRED"));
        long questionId = createdId(postJson("/api/questions", null,
                question.formatted(",\"email\":\"g@example.com\",\"phoneNumber\":\"010-0000-0000\""))
                .andExpect(status().isCreated()));
        postJson("/api/questions/" + questionId + "/access", null, "{\"password\":\"pw1234\"}")
                .andExpect(jsonPath("$.email").value("g@example.com"))
                .andExpect(jsonPath("$.phoneNumber").doesNotExist());

        putJson("/api/admin/questions/guest-posting", admin,
                "{\"allowed\":true,\"collectPhone\":false,\"collectEmail\":false}")
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        putJson("/api/admin/reports/guest-posting", admin,
                "{\"allowed\":true,\"collectPhone\":true,\"collectEmail\":false}")
                .andExpect(jsonPath("$.collectEmail").value(false));
        postJson("/api/reports", null, report.formatted(",\"email\":\"g@example.com\""))
                .andExpect(jsonPath("$.code").value("GUEST_CONTACT_REQUIRED"));
        postJson("/api/reports", null, report.formatted(",\"phoneNumber\":\"010-1111-2222\""))
                .andExpect(status().isCreated());

        putJson("/api/admin/questions/guest-posting", admin,
                "{\"allowed\":false,\"collectPhone\":false,\"collectEmail\":true}").andExpect(status().isOk());
        putJson("/api/admin/reports/guest-posting", admin,
                "{\"allowed\":false,\"collectPhone\":true,\"collectEmail\":true}").andExpect(status().isOk());
        putJson("/api/admin/reports/guest-posting", null,
                "{\"allowed\":true,\"collectPhone\":true,\"collectEmail\":true}").andExpect(status().isUnauthorized());
        getJson("/api/questions/guest-posting", null).andExpect(jsonPath("$.allowed").value(false));
        getJson("/api/reports/guest-posting", null).andExpect(jsonPath("$.allowed").value(false));
        postJson("/api/questions", null, question.formatted(",\"email\":\"g@example.com\""))
                .andExpect(jsonPath("$.code").value("GUEST_WRITE_NOT_ALLOWED"));
        postJson("/api/reports", null, report.formatted(",\"phoneNumber\":\"010-1234-5678\""))
                .andExpect(jsonPath("$.code").value("GUEST_WRITE_NOT_ALLOWED"));
        postJson("/api/questions", admin, question.formatted("")).andExpect(status().isCreated());
    }

    @Test
    void archiveMenusPointAtOneArchive() throws Exception {
        MockHttpSession admin = admin();
        long menu = createdId(postJson("/api/admin/menus", admin, """
                {"name":"공지","type":"ARCHIVE","targetId":3}
                """).andExpect(status().isCreated()));
        getJson("/api/admin/menus/" + menu, admin)
                .andExpect(jsonPath("$.type").value("ARCHIVE"))
                .andExpect(jsonPath("$.targetId").value(3));
        postJson("/api/admin/menus", admin, """
                {"name":"대상 없음","type":"ARCHIVE"}
                """).andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void hiddenMenusTakeTheirChildrenAlong() throws Exception {
        MockHttpSession admin = admin();
        long parent = createdId(postJson("/api/admin/menus", admin, """
                {"name":"숨김 부모","type":"LINK","link":"/a","isUsing":false}
                """).andExpect(status().isCreated()));
        postJson("/api/admin/menus", admin, """
                {"parentId":%d,"name":"자식","type":"LINK","link":"/b"}
                """.formatted(parent)).andExpect(status().isCreated());

        getJson("/api/menus", null)
                .andExpect(jsonPath("$[?(@.name == '숨김 부모')]").isEmpty())
                .andExpect(jsonPath("$..name").value(org.hamcrest.Matchers.not(hasItem("자식"))));
    }

    @Test
    void onlyActiveBannersArePublic() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/banners", admin, """
                {"type":"BANNER","name":"진행중","isUsing":true}
                """).andExpect(status().isCreated());
        postJson("/api/admin/banners", admin, """
                {"type":"BANNER","name":"종료","isUsing":true,"endDateTime":"%s"}
                """.formatted(LocalDateTime.now().minusDays(1))).andExpect(status().isCreated());
        postJson("/api/admin/banners", admin, """
                {"type":"BANNER","name":"미사용","isUsing":false}
                """).andExpect(status().isCreated());
        postJson("/api/admin/banners", admin, """
                {"type":"BANNER","name":"기간오류","isUsing":true,"startDateTime":"2026-02-01T00:00:00","endDateTime":"2026-01-01T00:00:00"}
                """).andExpect(jsonPath("$.code").value("INVALID_PERIOD"));

        getJson("/api/banners", null)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("진행중"));
    }

    @Test
    void topBarColorsAndPopupModesAreKept() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/banners", admin, """
                {"type":"BANNER","name":"점검 안내","description":"11/3 02시 점검","isUsing":true,
                 "backgroundColor":"#1D4ED8","textColor":"#FFFFFF"}
                """).andExpect(status().isCreated());
        postJson("/api/admin/banners", admin, """
                {"type":"POPUP","name":"필수 공지","isUsing":true,"isModal":true,"dismissDays":7}
                """).andExpect(status().isCreated());
        postJson("/api/admin/banners", admin, """
                {"type":"BANNER","name":"색상 오류","isUsing":true,"backgroundColor":"blue"}
                """).andExpect(jsonPath("$.fieldErrors[0].field").value("backgroundColor"));

        getJson("/api/banners?type=BANNER", null)
                .andExpect(jsonPath("$[0].backgroundColor").value("#1D4ED8"))
                .andExpect(jsonPath("$[0].textColor").value("#FFFFFF"));
        getJson("/api/banners?type=POPUP", null)
                .andExpect(jsonPath("$[0].isModal").value(true))
                .andExpect(jsonPath("$[0].dismissDays").value(7));
        getJson("/api/banners?type=BANNER", null).andExpect(jsonPath("$[0].dismissDays").value(1));
        postJson("/api/admin/banners", admin, """
                {"type":"POPUP","name":"기간 오류","isUsing":true,"dismissDays":400}
                """).andExpect(jsonPath("$.fieldErrors[0].field").value("dismissDays"));
    }

    @Test
    void adminChangesAreAudited() throws Exception {
        MockHttpSession admin = admin();
        postJson("/api/admin/menus", admin, """
                {"name":"감사로그","type":"LINK","link":"/x"}
                """).andExpect(status().isCreated());

        getJson("/api/admin/audit-logs?keyword=root", admin)
                .andExpect(jsonPath("$.content[*].uri").value(hasItem("POST /api/admin/menus")));
    }
}

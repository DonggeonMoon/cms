package com.dgmoonlabs.cms.domain.board;

import com.dgmoonlabs.cms.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BoardApiTest extends ApiTestSupport {
    private long createBoard(final MockHttpSession admin, final String flags) throws Exception {
        return createdId(postJson("/api/admin/boards", admin, """
                {"name":"자유게시판%d","type":"NORMAL","maximumFileCount":5,"pageSize":10,%s}
                """.formatted(System.nanoTime(), flags)).andExpect(status().isCreated()));
    }

    @Test
    void guestWritesSecretArticleAndOpensItWithPassword() throws Exception {
        long boardId = createBoard(admin(), """
                "isGuestAccessible":true,"isGuestReadable":true,"isGuestWritable":true,"supportsSecret":true,"supportsComment":true
                """);

        postJson("/api/boards/" + boardId + "/articles", null, """
                {"title":"이름 없음","contentHtml":"<p>x</p>"}
                """).andExpect(jsonPath("$.code").value("GUEST_PASSWORD_REQUIRED"));

        long articleId = createdId(postJson("/api/boards/" + boardId + "/articles", null, """
                {"title":"비밀 문의","contentHtml":"<p>안녕<script>alert(1)</script></p>","isSecret":true,
                 "authorName":"손님","password":"guestpw"}
                """).andExpect(status().isCreated()));

        String articleUrl = "/api/boards/" + boardId + "/articles/" + articleId;
        getJson(articleUrl, null).andExpect(jsonPath("$.code").value("ARTICLE_PASSWORD_MISMATCH"));
        postJson(articleUrl + "/access", null, "{\"password\":\"guestpw\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorName").value("손님"))
                .andExpect(jsonPath("$.contentHtml").value(not(containsString("<script"))));

        deleteJson(articleUrl, null, "{\"password\":\"wrong\"}").andExpect(status().isForbidden());
        deleteJson(articleUrl, null, "{\"password\":\"guestpw\"}").andExpect(status().isNoContent());
    }

    @Test
    void permissionsAndCommentsFollowBoardConfig() throws Exception {
        MockHttpSession admin = admin();
        long boardId = createBoard(admin, """
                "isMemberAccessible":true,"isMemberReadable":true,"isMemberWritable":true,
                "isGuestAccessible":true,"supportsComment":true,"supportsLike":true,"supportsNotice":true
                """);
        MockHttpSession alice = member("alice");
        MockHttpSession bob = member("bobby");

        postJson("/api/boards/" + boardId + "/articles", null, """
                {"title":"x","authorName":"a","password":"p"}
                """).andExpect(jsonPath("$.code").value("BOARD_PERMISSION_DENIED"));

        long articleId = createdId(postJson("/api/boards/" + boardId + "/articles", alice, """
                {"title":"앨리스 글","contentHtml":"<p>본문</p>"}
                """).andExpect(status().isCreated()));
        postJson("/api/boards/" + boardId + "/articles", admin, """
                {"title":"공지","isNotice":true}
                """).andExpect(status().isCreated());
        postJson("/api/boards/" + boardId + "/articles", alice, """
                {"title":"몰래 공지","isNotice":true}
                """).andExpect(jsonPath("$.code").value("FEATURE_NOT_SUPPORTED"));

        String articleUrl = "/api/boards/" + boardId + "/articles/" + articleId;
        getJson(articleUrl, null).andExpect(jsonPath("$.code").value("BOARD_PERMISSION_DENIED"));
        putJson(articleUrl, bob, "{\"title\":\"남의 글 수정\"}").andExpect(status().isForbidden());

        long commentId = createdId(postJson(articleUrl + "/comments", bob, "{\"content\":\"좋은 글\"}")
                .andExpect(status().isCreated()));
        postJson(articleUrl + "/comments", alice, "{\"parentId\":%d,\"content\":\"감사\"}".formatted(commentId))
                .andExpect(status().isCreated());
        deleteJson(articleUrl + "/comments/" + commentId, alice, null).andExpect(status().isForbidden());
        putJson(articleUrl + "/comments/" + commentId, alice, "{\"content\":\"남의 댓글 수정\"}").andExpect(status().isForbidden());
        putJson(articleUrl + "/comments/" + commentId, admin, "{\"content\":\"관리자 수정\"}").andExpect(status().isForbidden());
        putJson(articleUrl + "/comments/" + commentId, bob, "{\"content\":\"좋은 글 (수정)\"}").andExpect(status().isNoContent());

        postJson(articleUrl + "/likes", bob, "").andExpect(jsonPath("$.likes").value(1));
        postJson(articleUrl + "/likes", bob, "").andExpect(jsonPath("$.likes").value(1));

        getJson("/api/boards/" + boardId + "/articles", alice)
                .andExpect(jsonPath("$.notices[0].title").value("공지"))
                .andExpect(jsonPath("$.articles.content[0].title").value("앨리스 글"))
                .andExpect(jsonPath("$.articles.content[0].commentCount").value(2));

        getJson(articleUrl + "/comments", bob)
                .andExpect(jsonPath("$[0].canEdit").value(true))
                .andExpect(jsonPath("$[1].canEdit").value(false));
        getJson(articleUrl + "/comments", alice)
                .andExpect(jsonPath("$[0].content").value("좋은 글 (수정)"))
                .andExpect(jsonPath("$[1].depth").value(1));
    }

    @Test
    void secretQuestionIsAnsweredByAdmin() throws Exception {
        long questionId = createdId(postJson("/api/questions", null, """
                {"title":"환불 문의","contentHtml":"<p>언제?</p>","isSecret":true,"authorName":"손님","password":"pw1234",
                 "email":"guest@example.com"}
                """).andExpect(status().isCreated()));

        getJson("/api/questions", null).andExpect(jsonPath("$.content[0].answered").value(false));
        getJson("/api/questions/" + questionId, null).andExpect(status().isForbidden());

        MockHttpSession admin = admin();
        getJson("/api/admin/dashboard", admin).andExpect(jsonPath("$.unansweredQuestions").value(1));
        getJson("/api/questions?unanswered=true", null).andExpect(jsonPath("$.content.length()").value(1));
        putJson("/api/admin/questions/" + questionId + "/answer", admin, "{\"contentHtml\":\"<p>내일 처리됩니다</p>\"}")
                .andExpect(status().isNoContent());
        getJson("/api/admin/dashboard", admin).andExpect(jsonPath("$.unansweredQuestions").value(0));
        getJson("/api/questions?unanswered=true", null).andExpect(jsonPath("$.content.length()").value(0));
        getJson("/api/questions", null).andExpect(jsonPath("$.content.length()").value(1));

        postJson("/api/questions/" + questionId + "/access", null, "{\"password\":\"pw1234\"}")
                .andExpect(jsonPath("$.answer.contentHtml").value("<p>내일 처리됩니다</p>"))
                .andExpect(jsonPath("$.email").value("guest@example.com"));
        putJson("/api/questions/" + questionId, null, """
                {"title":"수정","password":"pw1234"}
                """).andExpect(jsonPath("$.code").value("ALREADY_ANSWERED"));
    }

    @Test
    void draftsAreHiddenFromVisitors() throws Exception {
        MockHttpSession admin = admin();
        long draft = createdId(postJson("/api/admin/contents", admin, """
                {"title":"회사 소개","contentHtml":"<h1>초안</h1>","status":"DRAFT"}
                """).andExpect(status().isCreated()));
        getJson("/api/contents/" + draft, null).andExpect(status().isNotFound());
        getJson("/api/contents", null).andExpect(jsonPath("$.length()").value(0));
        putJson("/api/admin/contents/" + draft, admin, """
                {"title":"회사 소개","contentHtml":"<h1>공개</h1>","status":"PUBLISHED"}
                """).andExpect(status().isNoContent());
        getJson("/api/contents/" + draft, null)
                .andExpect(jsonPath("$.contentHtml").value("<h1>공개</h1>"))
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.lastWorker").value("root"));
        getJson("/api/contents", null).andExpect(jsonPath("$[0].title").value("회사 소개"));

        long archiveId = createdId(postJson("/api/admin/archives", admin, """
                {"name":"자료실","type":"NORMAL","supportsComment":true,"pageSize":10}
                """).andExpect(status().isCreated()));
        long published = createdId(postJson("/api/admin/archives/" + archiveId + "/articles", admin, """
                {"title":"공개 자료","status":"PUBLISHED"}
                """).andExpect(status().isCreated()));
        long hidden = createdId(postJson("/api/admin/archives/" + archiveId + "/articles", admin, """
                {"title":"비공개 자료","status":"DRAFT"}
                """).andExpect(status().isCreated()));

        getJson("/api/archives/" + archiveId + "/articles", null)
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("공개 자료"));
        getJson("/api/archives/" + archiveId + "/articles/" + hidden, null).andExpect(status().isNotFound());
        getJson("/api/archives/" + archiveId + "/articles", admin).andExpect(jsonPath("$.page.totalElements").value(2));

        String commentsUrl = "/api/archives/" + archiveId + "/articles/" + published + "/comments";
        MockHttpSession carol = member("carol");
        long commentId = createdId(postJson(commentsUrl, carol, "{\"content\":\"유용해요\"}").andExpect(status().isCreated()));
        putJson(commentsUrl + "/" + commentId, admin, "{\"content\":\"관리자 수정\"}").andExpect(status().isForbidden());
        putJson(commentsUrl + "/" + commentId, carol, "{\"content\":\"정말 유용해요\"}").andExpect(status().isNoContent());
        getJson(commentsUrl, carol)
                .andExpect(jsonPath("$[0].content").value("정말 유용해요"))
                .andExpect(jsonPath("$[0].canEdit").value(true));

        MockHttpSession dave = member("dave1");
        long reply = createdId(postJson(commentsUrl, dave, "{\"parentId\":%d,\"content\":\"동감\"}".formatted(commentId))
                .andExpect(status().isCreated()));
        postJson(commentsUrl, carol, "{\"parentId\":%d,\"content\":\"답글의 답글\"}".formatted(reply))
                .andExpect(jsonPath("$.code").value("FEATURE_NOT_SUPPORTED"));
        deleteJson(commentsUrl + "/" + commentId, carol, null).andExpect(status().isNoContent());
        getJson(commentsUrl, dave)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].deleted").value(true))
                .andExpect(jsonPath("$[1].depth").value(1))
                .andExpect(jsonPath("$[1].parentId").value(commentId));

        String likeUrl = "/api/archives/" + archiveId + "/articles/" + published + "/likes";
        postJson(likeUrl, dave, "").andExpect(jsonPath("$.code").value("FEATURE_NOT_SUPPORTED"));
        putJson("/api/admin/archives/" + archiveId, admin, """
                {"name":"자료실","type":"NORMAL","supportsComment":true,"supportsLike":true,"pageSize":10}
                """).andExpect(status().isNoContent());
        postJson(likeUrl, null, "").andExpect(status().isUnauthorized());
        postJson(likeUrl, dave, "").andExpect(jsonPath("$.likes").value(1));
        postJson(likeUrl, dave, "").andExpect(jsonPath("$.likes").value(1));
        postJson(likeUrl, carol, "").andExpect(jsonPath("$.likes").value(2));
        getJson("/api/archives/" + archiveId + "/articles/" + published, null).andExpect(jsonPath("$.likes").value(2));
        getJson("/api/archives/" + archiveId, null).andExpect(jsonPath("$.supportsLike").value(true));
    }
}

package egovframework.com.cms.index.web;

import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.support.Constant;
import egovframework.com.cms.support.annotation.CurrentSite;
import egovframework.com.cms.support.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
@Slf4j
public class IndexAdminController {
    @GetMapping("/admin")
    public String indexAdminGet() {
        return "redirect:/site/main/admin/login.do";
    }

    @RequestMapping(value = Constant.APP_PATH + Constant.SITE_ID_PATH + Constant.ADMIN_PATH + ".do")
    public String adminShortGet(@CurrentSite Site currentSite) {
        return Utils.getAdminPath("redirect:", currentSite) + "/login.do";
    }

    @GetMapping(Constant.APP_PATH + Constant.SITE_ID_PATH + Constant.ADMIN_PATH + "/frame.do")
    public String frameGet() {
        return "admin/frame/frame";
    }

    @GetMapping(Constant.APP_PATH + Constant.SITE_ID_PATH + Constant.ADMIN_PATH + "/frame/top.do")
    public String frameTopGet() {
        return "admin/frame/top";
    }

    @GetMapping(Constant.APP_PATH + Constant.SITE_ID_PATH + Constant.ADMIN_PATH + "/frame/bottom.do")
    public String frameBottomGet() {
        return "admin/frame/bottom";
    }

    @GetMapping(Constant.APP_PATH + Constant.SITE_ID_PATH + Constant.ADMIN_PATH + "/frame/left.do")
    public String frameleftGet() {
        return "admin/frame/left";

    }

    @GetMapping(Constant.APP_PATH + Constant.SITE_ID_PATH + Constant.ADMIN_PATH + "/refreshDummy.do")
    public void adminRefreshDummyGet(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}

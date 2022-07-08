package egovframework.com.cms.index.web;

import egovframework.com.cms.support.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
@Slf4j
public class IndexAdminController {
    @GetMapping("/")
    public String indexRedirectGet() {
        return "redirect:/site/main/home";
    }

    @GetMapping(Constant.APP_PATH + Constant.SITE_ID_PATH + "/home")
    public String indexGet(HttpServletRequest request) {
        String theme = (String) request.getAttribute("theme");
        return "theme/" + theme + "/index/home";
    }
}

package egovframework.com.cms.faq.web;

import egovframework.com.cms.faq.dto.FaqSearch;
import egovframework.com.cms.faq.service.FaqService;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.support.Constant;
import egovframework.com.cms.support.Pagination.Paging;
import egovframework.com.cms.support.annotation.CurrentSite;
import egovframework.com.cms.support.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class FaqUserController {

    private final FaqService faqService;

    // 유저는 FAQ 조회만 가능
    @GetMapping(Constant.APP_PATH + Constant.SITE_ID_PATH + "/faq/list")
    public String listPageGet(HttpServletRequest request, @CurrentSite Site currentSite, @ModelAttribute FaqSearch faqSearch, Model model) throws Exception {

        faqSearch.setSitePrefix(currentSite.getSitePrefix());
        faqSearch.fixBrokenSvByDefaultCharsets();

        faqSearch.setPaging(true);
        faqSearch.setPublish(true);
        faqSearch.setPageSize(10);

        Paging paging = faqService.findPage(faqSearch);

        model.addAttribute("paging", paging);

        return Utils.getThemePath(request) + "faq/list";
    }
}

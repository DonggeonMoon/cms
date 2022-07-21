package egovframework.com.cms.site.service.impl;

import egovframework.com.cms.site.dto.SiteSearch;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.site.repository.SiteRepository;
import egovframework.com.cms.site.service.SiteService;
import egovframework.com.cms.support.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteServiceImpl implements SiteService {
    private final SiteRepository siteRepository;

    @Override
    public List<Site> getSiteList(SiteSearch siteSearch) {
        return siteRepository.findList(siteSearch);
    }

    @Override
    public Site detectCurrentSite(HttpServletRequest request) {

        List<Site> list = getSiteList(null);

        String[] temp = request.getRequestURI().split("/");
        String siteId = temp[2].toLowerCase();
        log.info("temp[0] : {}", temp[0]);
        log.info("temp[1] : {}", temp[1]);
        log.info("temp[2] : {}", temp[2]);
        log.info("siteId : {}", siteId);

        if (siteId.equals(Constant.MAIN_SITE_DISPLAY_ID)) {
            for (Site site : list) {
                if (site.isSiteMain()) {
                    return site; //main 사이트 반환
                }
            }
        } else {
            for (Site site : list) {
                if (siteId.equals(site.getSiteId())) {
                    return site; //  siteId가 2번째칸인 지 비교 해서 반환
                }
            }
        }

        //사이트 못찾으면 메인 사이트 반환
        for (Site site : list) {
            if (site.isSiteMain()) {
                return site;
            }
        }

        return null;
    }
}

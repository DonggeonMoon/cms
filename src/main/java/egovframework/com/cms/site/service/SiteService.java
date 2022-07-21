package egovframework.com.cms.site.service;

import egovframework.com.cms.site.dto.SiteSearch;
import egovframework.com.cms.site.model.Site;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface SiteService {
    List<Site> getSiteList(SiteSearch siteSearch);

    Site detectCurrentSite(HttpServletRequest request);
}

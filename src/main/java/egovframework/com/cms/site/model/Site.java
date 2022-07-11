package egovframework.com.cms.site.model;

import egovframework.com.cms.support.Constant;
import egovframework.com.cms.watchdog.aop.WatchDog;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Site {
    private static final String DOMAIN_GLUE = ",";

    @WatchDog
    private String siteId;
    private String siteDomain;
    private String siteName;
    private String siteDescription;
    private String siteTheme;
    private boolean siteMain;
    private String siteLocale;

    private String siteType = "domain";//서브사이트 구분유형 domain | dir

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteDomain() {
        return siteDomain;
    }

    public void setSiteDomain(String siteDomain) {
        this.siteDomain = siteDomain;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteDescription() {
        return siteDescription;
    }

    public void setSiteDescription(String siteDescription) {
        this.siteDescription = siteDescription;
    }

    public String getSiteTheme() {
        return siteTheme;
    }

    public void setSiteTheme(String siteTheme) {
        this.siteTheme = siteTheme;
    }

    public boolean isSiteMain() {
        return siteMain;
    }

    public void setSiteMain(boolean siteMain) {
        this.siteMain = siteMain;
    }

    public String getSiteLocale() {
        if (StringUtils.isBlank(this.siteLocale)) {
            return "ko_KR";
        }
        return siteLocale;
    }

    public void setSiteLocale(String siteLocale) {
        this.siteLocale = siteLocale;
    }

    public String getSiteType() {
        return siteType;
    }

    public void setSiteType(String siteType) {
        this.siteType = siteType;
    }

    public String getSitePrefix() {
        //메인사이트의 경우 테이블은 ECMS로 시작하고 데이터상의 사이트 아이디는 main 임
        if (Constant.MAIN_SITE_DISPLAY_ID.equals(this.getSiteId())) {
            return Constant.MAIN_SITE_PREFIX;
        }
        return this.getSiteId().toUpperCase();
    }

    public List<String> getSeperatedSiteDomains() {
        List<String> list = new ArrayList<String>();
        String domains = StringUtils.deleteWhitespace(this.getSiteDomain());
        if (StringUtils.isNotBlank(domains)) {
            if (!domains.contains(DOMAIN_GLUE)) {
                list.add(domains);
            } else {
                String[] temp = domains.split(DOMAIN_GLUE);
                for (String item : temp) {
                    list.add(item);
                }
            }
        }
        return list;
    }
}

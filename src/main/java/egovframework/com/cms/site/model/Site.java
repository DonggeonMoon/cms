package egovframework.com.cms.site.model;

import egovframework.com.cms.watchdog.aop.WatchDog;

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
}

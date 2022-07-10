package egovframework.com.cms.site.dto;

import org.codehaus.jackson.annotate.JsonIgnore;

public class MultiSiteVO {
    @JsonIgnore
    protected String sitePrefix;

    public String getSitePrefix() {
        return sitePrefix;
    }

    public void setSitePrefix(String sitePrefix) {
        this.sitePrefix = sitePrefix;
    }

    @JsonIgnore
    public String getSiteId() {
        return this.sitePrefix.toLowerCase();
    }
}

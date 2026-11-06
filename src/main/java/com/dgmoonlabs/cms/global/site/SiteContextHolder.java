package com.dgmoonlabs.cms.global.site;

public final class SiteContextHolder {
    private static final ThreadLocal<Long> CURRENT_SITE_ID = new ThreadLocal<>();

    private SiteContextHolder() {
    }

    public static void set(final Long siteId) {
        CURRENT_SITE_ID.set(siteId);
    }

    public static Long currentSiteId() {
        return CURRENT_SITE_ID.get();
    }

    public static void clear() {
        CURRENT_SITE_ID.remove();
    }
}

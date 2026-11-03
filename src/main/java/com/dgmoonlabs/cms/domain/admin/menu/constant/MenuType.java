package com.dgmoonlabs.cms.domain.admin.menu.constant;

import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public enum MenuType {
    CONTENT(SiteFeature.CONTENT),
    BOARD(SiteFeature.BOARD),
    ARCHIVE(SiteFeature.ARCHIVE),
    LINK(null),
    FEATURE(null);

    private final SiteFeature feature;

    MenuType(final SiteFeature feature) {
        this.feature = feature;
    }

    public Optional<SiteFeature> feature() {
        return Optional.ofNullable(feature);
    }
}

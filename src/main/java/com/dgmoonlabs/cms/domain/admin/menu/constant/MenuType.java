package com.dgmoonlabs.cms.domain.admin.menu.constant;

import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;

import java.util.Optional;

public enum MenuType {
    CONTENT(SiteFeature.CONTENT),
    BOARD(SiteFeature.BOARD),
    LINK(null),
    // a built-in menu for a whole feature (상품, 게시판, ...); created by the system, renamable and hideable but not deletable
    FEATURE(null);

    // the site feature a menu of this type leads to; LINK menus point anywhere and belong to none
    private final SiteFeature feature;

    MenuType(final SiteFeature feature) {
        this.feature = feature;
    }

    public Optional<SiteFeature> feature() {
        return Optional.ofNullable(feature);
    }
}

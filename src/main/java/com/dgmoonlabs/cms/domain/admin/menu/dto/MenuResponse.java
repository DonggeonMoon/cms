package com.dgmoonlabs.cms.domain.admin.menu.dto;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import com.dgmoonlabs.cms.domain.admin.menu.entity.Menu;
import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record MenuResponse(
        Long id,
        Long parentId,
        String name,
        MenuType type,
        int depth,
        int sequence,
        String link,
        Long targetId,
        SiteFeature feature,
        boolean isUsing,
        List<MenuResponse> children
) {
    public static MenuResponse from(final Menu menu) {
        return new MenuResponse(menu.getId(), menu.getParentId(), menu.getName(), menu.getType(), menu.getDepth(),
                menu.getSequence(), menu.getLink(), menu.getTargetId(), menu.getFeature(), menu.isUsing(), new ArrayList<>());
    }

    public static List<MenuResponse> tree(final List<Menu> menus) {
        Map<Long, MenuResponse> byId = new LinkedHashMap<>();
        menus.stream()
                .sorted(Comparator.comparingInt(Menu::getDepth).thenComparingInt(Menu::getSequence).thenComparing(Menu::getId))
                .forEach(menu -> byId.put(menu.getId(), from(menu)));

        List<MenuResponse> roots = new ArrayList<>();
        byId.values().forEach(node -> {
            MenuResponse parent = node.parentId() == null ? null : byId.get(node.parentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.children().add(node);
            }
        });
        return roots;
    }
}

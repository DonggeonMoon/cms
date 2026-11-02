package com.dgmoonlabs.cms.domain.admin.menu;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import com.dgmoonlabs.cms.domain.admin.menu.entity.Menu;
import com.dgmoonlabs.cms.domain.admin.menu.repository.MenuRepository;
import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeatureMenuInitializer implements ApplicationRunner {
    private static final Map<SiteFeature, String> DEFAULT_NAMES = new LinkedHashMap<>();

    static {
        DEFAULT_NAMES.put(SiteFeature.BOARD, "게시판");
        DEFAULT_NAMES.put(SiteFeature.ARCHIVE, "아카이브");
        DEFAULT_NAMES.put(SiteFeature.PRODUCT, "상품");
        DEFAULT_NAMES.put(SiteFeature.PROGRAM, "프로그램");
        DEFAULT_NAMES.put(SiteFeature.QNA, "질문답변");
        DEFAULT_NAMES.put(SiteFeature.REPORT, "신고");
        DEFAULT_NAMES.put(SiteFeature.MAP, "지도");
    }

    private final MenuRepository menuRepository;

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        List<Menu> menus = menuRepository.findAllByDeletedAtIsNull();
        menus.stream()
                .filter(menu -> menu.isFeatureMenu() && !DEFAULT_NAMES.containsKey(menu.getFeature()))
                .forEach(Menu::delete);
        menus = menus.stream().filter(menu -> menu.getDeletedAt() == null).toList();
        Set<SiteFeature> existing = menus.stream()
                .filter(Menu::isFeatureMenu)
                .map(Menu::getFeature)
                .collect(Collectors.toSet());
        int sequence = menus.stream()
                .filter(menu -> menu.getParentId() == null)
                .mapToInt(menu -> menu.getSequence() + 1)
                .max()
                .orElse(0);
        for (Map.Entry<SiteFeature, String> entry : DEFAULT_NAMES.entrySet()) {
            if (!existing.contains(entry.getKey())) {
                menuRepository.save(Menu.builder()
                        .name(entry.getValue())
                        .type(MenuType.FEATURE)
                        .feature(entry.getKey())
                        .sequence(sequence++)
                        .build());
            }
        }
    }
}

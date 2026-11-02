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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeatureMenuInitializer implements ApplicationRunner {
    private static final int FIRST_SEQUENCE = 100;
    private static final Map<SiteFeature, String> DEFAULT_NAMES = new LinkedHashMap<>();

    static {
        DEFAULT_NAMES.put(SiteFeature.CONTENT, "소개");
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
        Set<SiteFeature> existing = menuRepository.findAllByDeletedAtIsNull().stream()
                .filter(Menu::isFeatureMenu)
                .map(Menu::getFeature)
                .collect(Collectors.toSet());
        int sequence = FIRST_SEQUENCE;
        for (Map.Entry<SiteFeature, String> entry : DEFAULT_NAMES.entrySet()) {
            if (!existing.contains(entry.getKey())) {
                menuRepository.save(Menu.builder()
                        .name(entry.getValue())
                        .type(MenuType.FEATURE)
                        .feature(entry.getKey())
                        .sequence(sequence)
                        .build());
            }
            sequence++;
        }
    }
}

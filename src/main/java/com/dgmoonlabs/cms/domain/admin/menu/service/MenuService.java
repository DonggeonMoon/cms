package com.dgmoonlabs.cms.domain.admin.menu.service;

import com.dgmoonlabs.cms.domain.admin.menu.constant.MenuType;
import com.dgmoonlabs.cms.domain.admin.menu.dto.MenuOrderRequest;
import com.dgmoonlabs.cms.domain.admin.menu.dto.MenuResponse;
import com.dgmoonlabs.cms.domain.admin.menu.dto.MenuSaveRequest;
import com.dgmoonlabs.cms.domain.admin.menu.entity.Menu;
import com.dgmoonlabs.cms.domain.admin.menu.repository.MenuRepository;
import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import com.dgmoonlabs.cms.domain.admin.site.service.SiteService;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final SiteService siteService;

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenuTree() {
        return MenuResponse.tree(menuRepository.findAllByDeletedAtIsNull());
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getPublicMenuTree() {
        List<Menu> menus = menuRepository.findAllByDeletedAtIsNull();
        Map<Long, Menu> byId = menus.stream().collect(Collectors.toMap(Menu::getId, Function.identity()));
        return MenuResponse.tree(menus.stream().filter(menu -> isVisible(menu, byId)).toList());
    }

    private boolean isVisible(final Menu menu, final Map<Long, Menu> byId) {
        for (Menu current = menu; current != null; current = current.getParentId() == null ? null : byId.get(current.getParentId())) {
            if (!current.isUsing()) {
                return false;
            }
        }
        return true;
    }

    @Transactional(readOnly = true)
    public MenuResponse getMenu(final Long id) {
        return MenuResponse.from(findMenu(id));
    }

    @Transactional
    public Long createMenu(final MenuSaveRequest request) {
        requireFeatureEnabled(request);
        Menu parent = request.parentId() == null ? null : findMenu(request.parentId());
        if (parent != null && parent.isFeatureMenu()) {
            throw new BusinessException(ErrorCode.MENU_BUILT_IN);
        }
        int depth = parent == null ? 0 : parent.getDepth() + 1;
        Menu menu = Menu.builder()
                .parentId(request.parentId())
                .depth(depth)
                .name(request.name())
                .type(request.type())
                .sequence(request.sequence())
                .targetId(request.type() == MenuType.FEATURE || request.type() == MenuType.LINK ? null : request.targetId())
                .link(request.type() == MenuType.LINK ? request.link() : null)
                .feature(request.type() == MenuType.FEATURE ? request.feature() : null)
                .isUsing(request.isUsingOrDefault())
                .build();
        return menuRepository.save(menu).getId();
    }

    @Transactional
    public void updateMenu(final Long id, final MenuSaveRequest request) {
        Menu menu = findMenu(id);
        requireFeatureEnabled(request);
        if (request.type() == MenuType.FEATURE && menuRepository.existsByParentIdAndDeletedAtIsNull(id)) {
            throw new BusinessException(ErrorCode.MENU_BUILT_IN);
        }
        menu.update(request);
    }

    @Transactional
    public void reorderMenus(final MenuOrderRequest request) {
        Map<Long, Menu> siblings = menuRepository.findAllByDeletedAtIsNull().stream()
                .filter(menu -> Objects.equals(menu.getParentId(), request.parentId()))
                .collect(Collectors.toMap(Menu::getId, Function.identity()));
        if (request.ids().size() != siblings.size() || !siblings.keySet().equals(Set.copyOf(request.ids()))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        for (int i = 0; i < request.ids().size(); i++) {
            siblings.get(request.ids().get(i)).moveTo(i + 1);
        }
    }

    private void requireFeatureEnabled(final MenuSaveRequest request) {
        SiteFeature feature = request.requiredFeature();
        if (feature != null && !siteService.isEnabled(feature)) {
            throw new BusinessException(ErrorCode.MENU_FEATURE_DISABLED);
        }
    }

    @Transactional
    public void deleteMenu(final Long id) {
        Menu menu = findMenu(id);
        if (menuRepository.existsByParentIdAndDeletedAtIsNull(id)) {
            throw new BusinessException(ErrorCode.MENU_HAS_CHILDREN);
        }
        menu.delete();
    }

    private Menu findMenu(final Long id) {
        return menuRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }
}

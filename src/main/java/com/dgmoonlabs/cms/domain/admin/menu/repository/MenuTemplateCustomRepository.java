package com.dgmoonlabs.cms.domain.admin.menu.repository;

import com.dgmoonlabs.cms.domain.admin.menu.dto.MenuTemplateRequest;
import com.dgmoonlabs.cms.domain.admin.menu.entity.Menu;
import com.dgmoonlabs.cms.domain.admin.menu.entity.MenuTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MenuTemplateCustomRepository {
    Page<MenuTemplate> find(MenuTemplateRequest request, Pageable pageable);

    List<MenuTemplate> find(MenuTemplateRequest request);
}

package com.dgmoonlabs.cms.domain.admin.menu.repository;

import com.dgmoonlabs.cms.domain.admin.menu.dto.MenuRequest;
import com.dgmoonlabs.cms.domain.admin.menu.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MenuCustomRepository {
    Page<Menu> find(MenuRequest request, Pageable pageable);

    List<Menu> find(MenuRequest request);
}

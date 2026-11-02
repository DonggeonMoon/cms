package com.dgmoonlabs.cms.domain.admin.menu.controller;

import com.dgmoonlabs.cms.domain.admin.menu.dto.MenuOrderRequest;
import com.dgmoonlabs.cms.domain.admin.menu.dto.MenuResponse;
import com.dgmoonlabs.cms.domain.admin.menu.dto.MenuSaveRequest;
import com.dgmoonlabs.cms.domain.admin.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @GetMapping("/api/menus")
    public ResponseEntity<List<MenuResponse>> publicTree() {
        return ResponseEntity.ok(menuService.getPublicMenuTree());
    }

    @GetMapping("/api/admin/menus")
    public ResponseEntity<List<MenuResponse>> tree() {
        return ResponseEntity.ok(menuService.getMenuTree());
    }

    @GetMapping("/api/admin/menus/{id}")
    public ResponseEntity<MenuResponse> get(@PathVariable final Long id) {
        return ResponseEntity.ok(menuService.getMenu(id));
    }

    @PostMapping("/api/admin/menus")
    public ResponseEntity<Void> create(@RequestBody @Valid final MenuSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/admin/menus/" + menuService.createMenu(request))).build();
    }

    @PutMapping("/api/admin/menus/order")
    public ResponseEntity<Void> reorder(@RequestBody @Valid final MenuOrderRequest request) {
        menuService.reorderMenus(request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/admin/menus/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final MenuSaveRequest request) {
        menuService.updateMenu(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/menus/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        menuService.deleteMenu(id);
        return ResponseEntity.noContent().build();
    }
}

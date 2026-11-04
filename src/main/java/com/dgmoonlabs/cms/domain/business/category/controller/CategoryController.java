package com.dgmoonlabs.cms.domain.business.category.controller;

import com.dgmoonlabs.cms.domain.business.category.dto.CategoryDtos.CategoryResponse;
import com.dgmoonlabs.cms.domain.business.category.dto.CategoryDtos.CategorySaveRequest;
import com.dgmoonlabs.cms.domain.business.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/api/product-categories")
    public ResponseEntity<List<CategoryResponse>> categories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @PostMapping("/api/admin/product-categories")
    public ResponseEntity<Void> create(@RequestBody @Valid final CategorySaveRequest request) {
        return ResponseEntity.created(URI.create("/api/product-categories/" + categoryService.create(request))).build();
    }

    @PutMapping("/api/admin/product-categories/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final CategorySaveRequest request) {
        categoryService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/product-categories/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

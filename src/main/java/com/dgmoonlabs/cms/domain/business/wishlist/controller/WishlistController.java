package com.dgmoonlabs.cms.domain.business.wishlist.controller;

import com.dgmoonlabs.cms.domain.business.wishlist.service.WishlistService;
import com.dgmoonlabs.cms.domain.business.wishlist.service.WishlistService.WishlistEntry;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<PagedModel<WishlistEntry>> mine(@AuthenticationPrincipal final LoginUser user,
                                                          @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(wishlistService.getMine(user.id(), pageable)));
    }

    @GetMapping("/product-ids")
    public ResponseEntity<List<Long>> productIds(@AuthenticationPrincipal final LoginUser user) {
        return ResponseEntity.ok(wishlistService.getMyProductIds(user.id()));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Void> add(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long productId) {
        wishlistService.add(user.id(), productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long productId) {
        wishlistService.remove(user.id(), productId);
        return ResponseEntity.noContent().build();
    }
}

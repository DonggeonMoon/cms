package com.dgmoonlabs.cms.domain.business.wishlist.service;

import com.dgmoonlabs.cms.domain.business.order.dto.ProductDtos.ProductSummary;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.wishlist.entity.WishlistItem;
import com.dgmoonlabs.cms.domain.business.wishlist.repository.WishlistItemRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;

    public record WishlistEntry(Long productId, LocalDateTime wishedAt, ProductSummary product) {
    }

    @Transactional(readOnly = true)
    public Page<WishlistEntry> getMine(final Long memberId, final Pageable pageable) {
        Page<WishlistItem> items = wishlistItemRepository.findLiveByMemberId(memberId, pageable);
        Map<Long, Product> products = productRepository.findAllById(items.map(WishlistItem::getProductId).toList()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        return items.map(item -> new WishlistEntry(item.getProductId(), item.getCreatedAt(),
                ProductSummary.from(products.get(item.getProductId()))));
    }

    @Transactional(readOnly = true)
    public List<Long> getMyProductIds(final Long memberId) {
        return wishlistItemRepository.findProductIdsByMemberId(memberId);
    }

    @Transactional
    public void add(final Long memberId, final Long productId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId).orElseThrow(BusinessException::notFound);
        if (wishlistItemRepository.findByMemberIdAndProductId(memberId, product.getId()).isEmpty()) {
            wishlistItemRepository.save(WishlistItem.builder().memberId(memberId).productId(product.getId()).build());
        }
    }

    @Transactional
    public void remove(final Long memberId, final Long productId) {
        wishlistItemRepository.findByMemberIdAndProductId(memberId, productId).ifPresent(wishlistItemRepository::delete);
    }
}

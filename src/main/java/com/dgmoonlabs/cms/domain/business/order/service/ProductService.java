package com.dgmoonlabs.cms.domain.business.order.service;

import com.dgmoonlabs.cms.domain.business.discount.service.DiscountService;
import com.dgmoonlabs.cms.domain.business.order.dto.ProductDtos.*;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.entity.product.ProductOption;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductOptionRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.review.repository.ReviewRepository;
import com.dgmoonlabs.cms.domain.common.file.service.FileService;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.html.HtmlSanitizer;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductOptionRepository optionRepository;
    private final ReviewRepository reviewRepository;
    private final DiscountService discountService;
    private final FileService fileService;
    private final HtmlSanitizer htmlSanitizer;

    @Transactional(readOnly = true)
    public Page<ProductSummary> getProducts(final String keyword, final Pageable pageable) {
        String term = keyword == null ? "" : keyword;
        Page<Product> products = CurrentUser.isAdmin()
                ? productRepository.findAllByDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(term, pageable)
                : productRepository.findAllByIsEnabledTrueAndDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(term, pageable);
        return products.map(ProductSummary::from);
    }

    @Transactional(readOnly = true)
    public ProductDetail getProduct(final Long id) {
        Product product = findProduct(id);
        if (!product.isOnSale() && !CurrentUser.isAdmin()) {
            throw BusinessException.notFound();
        }
        return ProductDetail.of(
                product,
                optionRepository.findAllByProductIdAndDeletedAtIsNullOrderByIdAsc(id).stream().map(OptionResponse::from).toList(),
                discountService.getApplicable(id),
                reviewRepository.averageRating(id)
        );
    }

    @Transactional
    public Long createProduct(final ProductSaveRequest request) {
        requireImage(request.imageFileId());
        String html = htmlSanitizer.sanitize(request.contentHtml());
        Product product = productRepository.save(Product.builder()
                .name(request.name())
                .description(request.description())
                .imageFileId(request.imageFileId())
                .price(request.price())
                .availableQuantity(request.availableQuantity())
                .contentHtml(html)
                .contentText(htmlSanitizer.toPlainText(html))
                .isEnabled(request.isEnabled())
                .build());
        syncOptions(product.getId(), request.optionsOrEmpty());
        return product.getId();
    }

    @Transactional
    public void updateProduct(final Long id, final ProductSaveRequest request) {
        requireImage(request.imageFileId());
        String html = htmlSanitizer.sanitize(request.contentHtml());
        findProduct(id).update(request.name(), request.description(), request.imageFileId(), request.price(),
                request.availableQuantity(), html, htmlSanitizer.toPlainText(html), request.isEnabled());
        syncOptions(id, request.optionsOrEmpty());
    }

    @Transactional
    public void deleteProduct(final Long id) {
        findProduct(id).delete();
    }

    public Product findProduct(final Long id) {
        return productRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    // options are snapshots: the old rows are soft-deleted (past orders still point at them) and the list is re-created
    private void syncOptions(final Long productId, final List<OptionRequest> requests) {
        optionRepository.findAllByProductIdAndDeletedAtIsNullOrderByIdAsc(productId).forEach(ProductOption::delete);
        for (OptionRequest request : requests) {
            optionRepository.save(ProductOption.builder()
                    .productId(productId)
                    .name(request.name())
                    .description(request.description())
                    .additionalPrice(request.additionalPrice())
                    .build());
        }
    }

    private void requireImage(final Long fileId) {
        if (fileId != null) {
            fileService.requireFilesExist(List.of(fileId));
        }
    }
}

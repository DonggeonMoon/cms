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
import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
import com.dgmoonlabs.cms.domain.business.order.dto.ProductDtos.DonationProgress;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.domain.common.grade.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductOptionRepository optionRepository;
    private final ReviewRepository reviewRepository;
    private final DiscountService discountService;
    private final FileService fileService;
    private final HtmlSanitizer htmlSanitizer;
    private final OrderRepository orderRepository;
    private final GradeService gradeService;

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
                reviewRepository.averageRating(id),
                product.getSaleType() == ProductSaleType.DONATION
                        ? new DonationProgress(orderRepository.sumPaidDonations(id), orderRepository.countPaidDonations(id))
                        : null
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
        changeSaleType(product, request);
        syncOptions(product, request.optionsOrEmpty());
        return product.getId();
    }

    @Transactional
    public void updateProduct(final Long id, final ProductSaveRequest request) {
        requireImage(request.imageFileId());
        String html = htmlSanitizer.sanitize(request.contentHtml());
        Product product = findProduct(id);
        product.update(request.name(), request.description(), request.imageFileId(), request.price(),
                request.availableQuantity(), html, htmlSanitizer.toPlainText(html), request.isEnabled());
        changeSaleType(product, request);
        syncOptions(product, request.optionsOrEmpty());
    }

    @Transactional
    public void deleteProduct(final Long id) {
        findProduct(id).delete();
    }

    public Product findProduct(final Long id) {
        return productRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private void changeSaleType(final Product product, final ProductSaveRequest request) {
        ProductSaleType saleType = request.saleTypeOrDefault();
        if (saleType == ProductSaleType.SUBSCRIPTION && request.billingCycle() == null) {
            throw new BusinessException(ErrorCode.BILLING_CYCLE_REQUIRED);
        }
        product.changeSaleType(saleType, request.billingCycle(), request.donationPresetsText(), request.goalAmount());
        product.changeGrades(gradeService.validGradeId(request.accessGradeId()),
                gradeService.validGradeId(request.grantGradeId()), request.grantGradeDays());
        product.changeShipping(request.requiresShippingOrDefault());
        product.changeOptionGroups(request.optionGroupsOrEmpty().stream().map(OptionGroupRequest::toGroup).toList());
    }

    /** 기존 옵션은 ID로 찾아 고쳐서, 주문·장바구니가 가리키는 옵션 ID와 조합별 재고를 유지한다. */
    private void syncOptions(final Product product, final List<OptionRequest> requests) {
        List<Product.OptionGroup> groups = product.getOptionGroups();
        validateCombinations(groups, requests);
        Map<Long, ProductOption> existing = optionRepository.findAllByProductIdAndDeletedAtIsNullOrderByIdAsc(product.getId())
                .stream().collect(Collectors.toMap(ProductOption::getId, option -> option));
        for (OptionRequest request : requests) {
            List<String> values = groups == null ? null : request.optionValues().stream().map(String::trim).toList();
            String name = values == null ? request.name() : String.join(" / ", values);
            ProductOption option = request.id() == null ? null : existing.remove(request.id());
            if (option == null) {
                optionRepository.save(ProductOption.builder()
                        .productId(product.getId())
                        .name(name)
                        .description(request.description())
                        .additionalPrice(request.additionalPrice())
                        .optionValues(values)
                        .stock(request.stock())
                        .isEnabled(request.isEnabledOrDefault())
                        .build());
            } else {
                option.update(name, request.description(), request.additionalPrice(), values, request.stock(),
                        request.isEnabledOrDefault());
            }
        }
        existing.values().forEach(ProductOption::delete);
    }

    private static void validateCombinations(final List<Product.OptionGroup> groups, final List<OptionRequest> requests) {
        if (groups == null) {
            if (requests.stream().anyMatch(request -> request.name() == null || request.name().isBlank())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
            return;
        }
        Set<List<String>> seen = new HashSet<>();
        for (OptionRequest request : requests) {
            List<String> values = request.optionValues() == null ? List.of() : request.optionValues().stream().map(String::trim).toList();
            boolean matches = values.size() == groups.size();
            for (int i = 0; matches && i < values.size(); i++) {
                matches = groups.get(i).values().contains(values.get(i));
            }
            if (!matches || !seen.add(values)) {
                throw new BusinessException(ErrorCode.INVALID_OPTION_COMBINATION);
            }
        }
    }

    private void requireImage(final Long fileId) {
        if (fileId != null) {
            fileService.requireFilesExist(List.of(fileId));
        }
    }
}

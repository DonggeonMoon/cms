package com.dgmoonlabs.cms.domain.business.stock.service;

import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.entity.product.ProductOption;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductOptionRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.stock.constant.StockMovementType;
import com.dgmoonlabs.cms.domain.business.stock.dto.StockMovementDtos.AdjustRequest;
import com.dgmoonlabs.cms.domain.business.stock.dto.StockMovementDtos.StockMovementResponse;
import com.dgmoonlabs.cms.domain.business.stock.entity.StockMovement;
import com.dgmoonlabs.cms.domain.business.stock.repository.StockMovementRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 재고 변동 이력. 주문 흐름(OrderService)에서 재고가 바뀔 때마다 이력을 남기고, 관리자는 실사 결과를 수동 조정으로 반영한다. */
@Service
@RequiredArgsConstructor
public class StockMovementService {
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository optionRepository;

    /** OrderService 등에서 재고가 바뀐 "뒤"에 호출한다 (quantityAfter는 호출 시점의 최신 재고를 그대로 기록). */
    @Transactional
    public void record(final Long productId, final Long productOptionId, final StockMovementType type,
                       final int quantity, final Long orderId, final String note) {
        if (quantity <= 0) {
            return;
        }
        Integer quantityAfter = productOptionId != null
                ? optionRepository.findById(productOptionId).map(ProductOption::getStock).orElse(null)
                : productRepository.findById(productId).map(Product::getAvailableQuantity).orElse(null);
        stockMovementRepository.save(StockMovement.builder()
                .productId(productId)
                .productOptionId(productOptionId)
                .type(type)
                .quantity(quantity)
                .orderId(orderId)
                .quantityAfter(quantityAfter)
                .note(note)
                .build());
    }

    /** 관리자가 실사 결과 등으로 재고를 직접 조정한다 (delta 양수=입고, 음수=출고). */
    @Transactional
    public StockMovementResponse adjust(final AdjustRequest request) {
        Product product = productRepository.findForUpdate(request.productId()).orElseThrow(BusinessException::notFound);
        ProductOption option = request.productOptionId() == null ? null
                : optionRepository.findForUpdate(request.productOptionId()).orElseThrow(BusinessException::notFound);
        int delta = request.delta();
        if (delta > 0) {
            product.increaseStock(delta);
            if (option != null) {
                option.increaseStock(delta);
            }
        } else if (delta < 0) {
            product.decreaseStock(-delta);
            if (option != null) {
                option.decreaseStock(-delta);
            }
        }
        Integer quantityAfter = option != null ? option.getStock() : product.getAvailableQuantity();
        StockMovement saved = stockMovementRepository.save(StockMovement.builder()
                .productId(product.getId())
                .productOptionId(option == null ? null : option.getId())
                .type(StockMovementType.ADJUST)
                .quantity(Math.abs(delta))
                .quantityAfter(quantityAfter)
                .note(request.note())
                .build());
        return StockMovementResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> search(final Long productId, final Pageable pageable) {
        return stockMovementRepository.findAllByProductIdOrderByIdDesc(productId, pageable).map(StockMovementResponse::from);
    }
}

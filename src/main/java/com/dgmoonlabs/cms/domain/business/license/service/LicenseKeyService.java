package com.dgmoonlabs.cms.domain.business.license.service;

import com.dgmoonlabs.cms.domain.business.license.dto.LicenseKeyDtos.BulkAddRequest;
import com.dgmoonlabs.cms.domain.business.license.dto.LicenseKeyDtos.BulkAddResult;
import com.dgmoonlabs.cms.domain.business.license.dto.LicenseKeyDtos.LicenseKeyResponse;
import com.dgmoonlabs.cms.domain.business.license.dto.LicenseKeyDtos.MyLicenseKeyResponse;
import com.dgmoonlabs.cms.domain.business.license.entity.LicenseKey;
import com.dgmoonlabs.cms.domain.business.license.repository.LicenseKeyRepository;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LicenseKeyService {
    private final LicenseKeyRepository licenseKeyRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public BulkAddResult addBulk(final BulkAddRequest request) {
        List<String> trimmed = request.codes().stream().map(String::trim).filter(code -> !code.isEmpty()).distinct().toList();
        Set<String> existing = Set.copyOf(licenseKeyRepository.findAllByCodeIn(trimmed)).stream()
                .map(LicenseKey::getCode).collect(Collectors.toSet());
        List<LicenseKey> toSave = trimmed.stream().filter(code -> !existing.contains(code))
                .map(code -> LicenseKey.builder().productId(request.productId()).code(code).build()).toList();
        licenseKeyRepository.saveAll(toSave);
        return new BulkAddResult(toSave.size(), trimmed.size() - toSave.size());
    }

    @Transactional(readOnly = true)
    public Page<LicenseKeyResponse> search(final Long productId, final Pageable pageable) {
        return licenseKeyRepository.findAllByProductIdOrderByIdAsc(productId, pageable).map(LicenseKeyResponse::from);
    }

    @Transactional(readOnly = true)
    public long availableCount(final Long productId) {
        return licenseKeyRepository.countByProductIdAndOrderIdIsNull(productId);
    }

    @Transactional
    public void remove(final Long id) {
        LicenseKey key = licenseKeyRepository.findById(id).orElseThrow(BusinessException::notFound);
        if (key.isAssigned()) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        licenseKeyRepository.delete(key);
    }

    @Transactional(readOnly = true)
    public Page<MyLicenseKeyResponse> getMine(final Long memberId, final Pageable pageable) {
        Page<LicenseKey> page = licenseKeyRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable);
        Map<Long, String> productNames = productRepository.findAllById(page.map(LicenseKey::getProductId).toList()).stream()
                .collect(Collectors.toMap(Product::getId, Product::getName));
        return page.map(key -> new MyLicenseKeyResponse(key.getId(), key.getProductId(),
                productNames.getOrDefault(key.getProductId(), ""), key.getCode(), key.getOrderId(), key.getAssignedAt()));
    }

    /** 결제가 끝나면 그 상품에 등록된 키 중 안 쓴 것을 주문 수량만큼 배정한다(키가 모자라면 있는 만큼만). */
    @EventListener
    @Transactional
    public void assignOnPayment(final OrderPaidEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null || order.getUserType() != UserType.MEMBER || order.getProductId() == null) {
            return;
        }
        for (int i = 0; i < order.getQuantity(); i++) {
            licenseKeyRepository.findFirstAvailable(order.getProductId())
                    .ifPresent(key -> key.assign(order.getId(), order.getUserId()));
        }
    }
}

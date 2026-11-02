package com.dgmoonlabs.cms.domain.business.discount.service;

import com.dgmoonlabs.cms.domain.business.discount.dto.DiscountDtos.DiscountResponse;
import com.dgmoonlabs.cms.domain.business.discount.dto.DiscountDtos.DiscountSaveRequest;
import com.dgmoonlabs.cms.domain.business.discount.entity.Discount;
import com.dgmoonlabs.cms.domain.business.discount.repository.DiscountRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountService {
    private final DiscountRepository discountRepository;

    @Transactional(readOnly = true)
    public Page<DiscountResponse> getDiscounts(final Pageable pageable) {
        return discountRepository.findAllByDeletedAtIsNullOrderByIdDesc(pageable).map(DiscountResponse::from);
    }

    @Transactional(readOnly = true)
    public List<DiscountResponse> getApplicable(final Long productId) {
        return discountRepository.findApplicable(productId).stream().map(DiscountResponse::from).toList();
    }

    @Transactional
    public Long createDiscount(final DiscountSaveRequest request) {
        return discountRepository.save(Discount.builder()
                .productId(request.productId())
                .name(request.name())
                .description(request.description())
                .rate(request.rate())
                .isUsing(request.isUsing())
                .build()).getId();
    }

    @Transactional
    public void updateDiscount(final Long id, final DiscountSaveRequest request) {
        findDiscount(id).update(request.productId(), request.name(), request.description(), request.rate(), request.isUsing());
    }

    @Transactional
    public void deleteDiscount(final Long id) {
        findDiscount(id).delete();
    }

    public BigDecimal applyTo(final Long discountId, final Long productId, final BigDecimal amount) {
        if (discountId == null) {
            return amount;
        }
        Discount discount = findDiscount(discountId);
        if (!discount.isApplicableTo(productId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return discount.apply(amount);
    }

    private Discount findDiscount(final Long id) {
        return discountRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }
}

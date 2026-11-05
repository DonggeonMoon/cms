package com.dgmoonlabs.cms.domain.business.currency.service;

import com.dgmoonlabs.cms.domain.business.currency.dto.ExchangeRateDtos.ExchangeRateRequest;
import com.dgmoonlabs.cms.domain.business.currency.dto.ExchangeRateDtos.ExchangeRateResponse;
import com.dgmoonlabs.cms.domain.business.currency.entity.ExchangeRate;
import com.dgmoonlabs.cms.domain.business.currency.repository.ExchangeRateRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 다통화 표시용 환율. 결제·가격 계산에는 관여하지 않고, 상품가를 안내용으로 환산해서 보여줄 때만 쓴다. */
@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    private final ExchangeRateRepository exchangeRateRepository;

    @Transactional(readOnly = true)
    public List<ExchangeRateResponse> search() {
        return exchangeRateRepository.findAllByOrderByCurrencyCodeAsc().stream().map(ExchangeRateResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ExchangeRateResponse> publicList() {
        return exchangeRateRepository.findAllByEnabledTrueOrderByCurrencyCodeAsc().stream().map(ExchangeRateResponse::from).toList();
    }

    @Transactional
    public ExchangeRateResponse upsert(final ExchangeRateRequest request) {
        String code = request.currencyCode().trim().toUpperCase();
        ExchangeRate entity = exchangeRateRepository.findByCurrencyCode(code)
                .orElseGet(() -> ExchangeRate.builder().currencyCode(code).build());
        entity.update(request.rate(), request.enabled());
        return ExchangeRateResponse.from(exchangeRateRepository.save(entity));
    }

    @Transactional
    public void remove(final Long id) {
        exchangeRateRepository.findById(id).orElseThrow(BusinessException::notFound);
        exchangeRateRepository.deleteById(id);
    }
}

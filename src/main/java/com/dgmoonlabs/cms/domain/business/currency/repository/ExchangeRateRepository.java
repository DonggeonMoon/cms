package com.dgmoonlabs.cms.domain.business.currency.repository;

import com.dgmoonlabs.cms.domain.business.currency.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    List<ExchangeRate> findAllByOrderByCurrencyCodeAsc();

    List<ExchangeRate> findAllByEnabledTrueOrderByCurrencyCodeAsc();

    Optional<ExchangeRate> findByCurrencyCode(String currencyCode);
}

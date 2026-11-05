package com.dgmoonlabs.cms.domain.business.currency.dto;

import com.dgmoonlabs.cms.domain.business.currency.entity.ExchangeRate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class ExchangeRateDtos {
    private ExchangeRateDtos() {
    }

    public record ExchangeRateRequest(@NotBlank @Size(max = 3) String currencyCode, @NotNull @Positive BigDecimal rate,
                                      boolean enabled) {
    }

    public record ExchangeRateResponse(Long id, String currencyCode, BigDecimal rate, boolean enabled) {
        public static ExchangeRateResponse from(final ExchangeRate entity) {
            return new ExchangeRateResponse(entity.getId(), entity.getCurrencyCode(), entity.getRate(), entity.isEnabled());
        }
    }
}

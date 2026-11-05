package com.dgmoonlabs.cms.domain.business.tax.dto;

import com.dgmoonlabs.cms.domain.business.tax.entity.TaxSetting;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public final class TaxDtos {
    private TaxDtos() {
    }

    public record TaxSettingRequest(@NotNull @PositiveOrZero BigDecimal vatRate, boolean priceIncludesVat, String customsNotice) {
    }

    public record TaxSettingResponse(BigDecimal vatRate, boolean priceIncludesVat, String customsNotice) {
        public static TaxSettingResponse from(final TaxSetting setting) {
            return new TaxSettingResponse(setting.getVatRate(), setting.isPriceIncludesVat(), setting.getCustomsNotice());
        }
    }
}

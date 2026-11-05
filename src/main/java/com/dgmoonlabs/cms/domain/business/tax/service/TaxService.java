package com.dgmoonlabs.cms.domain.business.tax.service;

import com.dgmoonlabs.cms.domain.business.tax.dto.TaxDtos.TaxSettingRequest;
import com.dgmoonlabs.cms.domain.business.tax.dto.TaxDtos.TaxSettingResponse;
import com.dgmoonlabs.cms.domain.business.tax.entity.TaxSetting;
import com.dgmoonlabs.cms.domain.business.tax.repository.TaxSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaxService {
    private final TaxSettingRepository taxSettingRepository;

    @Transactional(readOnly = true)
    public TaxSettingResponse getSetting() {
        return TaxSettingResponse.from(setting());
    }

    @Transactional
    public TaxSettingResponse update(final TaxSettingRequest request) {
        TaxSetting setting = taxSettingRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> taxSettingRepository.save(TaxSetting.builder().build()));
        setting.update(request.vatRate(), request.priceIncludesVat(),
                request.customsNotice() == null || request.customsNotice().isBlank() ? null : request.customsNotice().trim());
        return TaxSettingResponse.from(setting);
    }

    private TaxSetting setting() {
        return taxSettingRepository.findFirstByOrderByIdAsc().orElseGet(() -> TaxSetting.builder().build());
    }
}

package com.dgmoonlabs.cms.domain.business.store.service;

import com.dgmoonlabs.cms.domain.business.order.constant.FulfillmentType;
import com.dgmoonlabs.cms.domain.business.store.dto.StoreDtos.StoreSettingRequest;
import com.dgmoonlabs.cms.domain.business.store.dto.StoreDtos.StoreSettingResponse;
import com.dgmoonlabs.cms.domain.business.store.entity.StoreSetting;
import com.dgmoonlabs.cms.domain.business.store.repository.StoreSettingRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreSettingRepository settingRepository;

    @Transactional(readOnly = true)
    public StoreSettingResponse getSetting() {
        StoreSetting setting = current();
        return StoreSettingResponse.of(setting, setting.isOpenAt(LocalDateTime.now()));
    }

    @Transactional
    public StoreSettingResponse saveSetting(final StoreSettingRequest request) {
        StoreSetting setting = settingRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> settingRepository.save(StoreSetting.builder().build()));
        List<String> tables = request.tables() == null ? List.of() : request.tables().stream().map(String::trim).distinct().toList();
        if (request.dineInEnabled() && tables.isEmpty()) {
            throw new BusinessException(ErrorCode.TABLE_REQUIRED);
        }
        setting.update(request.pickupEnabled(), request.dineInEnabled(), tables, request.openTime(), request.closeTime(),
                request.closedDays() == null ? List.of() : request.closedDays().stream().distinct().toList(), request.paused(),
                request.notice() == null || request.notice().isBlank() ? null : request.notice().trim());
        return StoreSettingResponse.of(setting, setting.isOpenAt(LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public String requireOrderable(final FulfillmentType type, final String tableName) {
        StoreSetting setting = current();
        boolean allowed = type == FulfillmentType.PICKUP ? setting.isPickupEnabled() : setting.isDineInEnabled();
        if (!allowed) {
            throw new BusinessException(ErrorCode.FULFILLMENT_NOT_AVAILABLE);
        }
        if (!setting.isOpenAt(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.STORE_CLOSED);
        }
        if (type == FulfillmentType.PICKUP) {
            return null;
        }
        String table = tableName == null ? "" : tableName.trim();
        if (!setting.tablesOrEmpty().contains(table)) {
            throw new BusinessException(ErrorCode.TABLE_REQUIRED);
        }
        return table;
    }

    private StoreSetting current() {
        return settingRepository.findFirstByOrderByIdAsc().orElseGet(() -> StoreSetting.builder().build());
    }
}

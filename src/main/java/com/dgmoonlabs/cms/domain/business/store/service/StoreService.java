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

import java.math.BigDecimal;
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
        List<String> zones = request.deliveryZones() == null ? List.of()
                : request.deliveryZones().stream().map(String::trim).distinct().toList();
        if (request.localDeliveryEnabled() && zones.isEmpty()) {
            throw new BusinessException(ErrorCode.DELIVERY_ZONE_NOT_SUPPORTED);
        }
        setting.update(request.pickupEnabled(), request.dineInEnabled(), tables, request.openTime(), request.closeTime(),
                request.closedDays() == null ? List.of() : request.closedDays().stream().distinct().toList(), request.paused(),
                request.notice() == null || request.notice().isBlank() ? null : request.notice().trim(),
                request.localDeliveryEnabled(), zones, request.deliveryFee(), request.deliveryMinimumAmount(),
                request.deliverySlotMinutes());
        return StoreSettingResponse.of(setting, setting.isOpenAt(LocalDateTime.now()));
    }

    /** 매장 주문(포장·매장 식사·배달)을 받을 수 있는지 확인하고, 매장 식사면 테이블 이름을 받아 돌려준다. */
    @Transactional(readOnly = true)
    public String requireOrderable(final FulfillmentType type, final String tableName) {
        StoreSetting setting = current();
        boolean allowed = switch (type) {
            case PICKUP -> setting.isPickupEnabled();
            case DINE_IN -> setting.isDineInEnabled();
            case LOCAL_DELIVERY -> setting.isLocalDeliveryEnabled();
            default -> false;
        };
        if (!allowed) {
            throw new BusinessException(ErrorCode.FULFILLMENT_NOT_AVAILABLE);
        }
        if (!setting.isOpenAt(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.STORE_CLOSED);
        }
        if (type != FulfillmentType.DINE_IN) {
            return null;
        }
        String table = tableName == null ? "" : tableName.trim();
        if (!setting.tablesOrEmpty().contains(table)) {
            throw new BusinessException(ErrorCode.TABLE_REQUIRED);
        }
        return table;
    }

    @Transactional(readOnly = true)
    public void requireDeliveryZone(final String postalCode) {
        StoreSetting setting = current();
        String code = postalCode == null ? "" : postalCode.trim();
        if (code.isEmpty() || setting.deliveryZonesOrEmpty().stream().noneMatch(code::startsWith)) {
            throw new BusinessException(ErrorCode.DELIVERY_ZONE_NOT_SUPPORTED);
        }
    }

    @Transactional(readOnly = true)
    public void requireDeliverySlot(final LocalDateTime slotAt) {
        Integer minutes = current().getDeliverySlotMinutes();
        if (minutes == null) {
            return;
        }
        if (slotAt == null || slotAt.isBefore(LocalDateTime.now()) || slotAt.getMinute() % minutes != 0) {
            throw new BusinessException(ErrorCode.INVALID_DELIVERY_SLOT);
        }
    }

    /** 배달 최소 주문 금액을 넘었는지 확인하고 배달 요금을 돌려준다. */
    @Transactional(readOnly = true)
    public BigDecimal deliveryFeeFor(final BigDecimal itemsAmount) {
        StoreSetting setting = current();
        BigDecimal minimum = setting.getDeliveryMinimumAmount();
        if (minimum != null && itemsAmount.compareTo(minimum) < 0) {
            throw new BusinessException(ErrorCode.DELIVERY_MINIMUM_NOT_MET);
        }
        return setting.deliveryFeeOrZero();
    }

    private StoreSetting current() {
        return settingRepository.findFirstByOrderByIdAsc().orElseGet(() -> StoreSetting.builder().build());
    }
}

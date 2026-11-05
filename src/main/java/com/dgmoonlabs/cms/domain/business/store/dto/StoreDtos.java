package com.dgmoonlabs.cms.domain.business.store.dto;

import com.dgmoonlabs.cms.domain.business.store.entity.StoreSetting;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public final class StoreDtos {
    private StoreDtos() {
    }

    public record StoreSettingRequest(boolean pickupEnabled, boolean dineInEnabled,
                                      @Size(max = 200) List<@NotBlank @Size(max = 30) String> tables,
                                      LocalTime openTime, LocalTime closeTime, List<DayOfWeek> closedDays, boolean paused,
                                      @Size(max = 300) String notice, boolean localDeliveryEnabled,
                                      @Size(max = 100) List<@NotBlank @Size(max = 10) String> deliveryZones,
                                      @PositiveOrZero BigDecimal deliveryFee, @PositiveOrZero BigDecimal deliveryMinimumAmount,
                                      @Positive Integer deliverySlotMinutes) {
    }

    public record StoreSettingResponse(boolean pickupEnabled, boolean dineInEnabled, List<String> tables, LocalTime openTime,
                                       LocalTime closeTime, List<DayOfWeek> closedDays, boolean paused, String notice,
                                       boolean localDeliveryEnabled, List<String> deliveryZones, BigDecimal deliveryFee,
                                       BigDecimal deliveryMinimumAmount, Integer deliverySlotMinutes, boolean openNow) {
        public static StoreSettingResponse of(final StoreSetting setting, final boolean openNow) {
            return new StoreSettingResponse(setting.isPickupEnabled(), setting.isDineInEnabled(), setting.tablesOrEmpty(),
                    setting.getOpenTime(), setting.getCloseTime(), setting.getClosedDays() == null ? List.of() : setting.getClosedDays(),
                    setting.isPaused(), setting.getNotice(), setting.isLocalDeliveryEnabled(), setting.deliveryZonesOrEmpty(),
                    setting.getDeliveryFee(), setting.getDeliveryMinimumAmount(), setting.getDeliverySlotMinutes(), openNow);
        }
    }
}

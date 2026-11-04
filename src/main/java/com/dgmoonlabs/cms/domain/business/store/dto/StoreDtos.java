package com.dgmoonlabs.cms.domain.business.store.dto;

import com.dgmoonlabs.cms.domain.business.store.entity.StoreSetting;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public final class StoreDtos {
    private StoreDtos() {
    }

    public record StoreSettingRequest(boolean pickupEnabled, boolean dineInEnabled,
                                      @Size(max = 200) List<@NotBlank @Size(max = 30) String> tables,
                                      LocalTime openTime, LocalTime closeTime, List<DayOfWeek> closedDays, boolean paused,
                                      @Size(max = 300) String notice) {
    }

    public record StoreSettingResponse(boolean pickupEnabled, boolean dineInEnabled, List<String> tables, LocalTime openTime,
                                       LocalTime closeTime, List<DayOfWeek> closedDays, boolean paused, String notice,
                                       boolean openNow) {
        public static StoreSettingResponse of(final StoreSetting setting, final boolean openNow) {
            return new StoreSettingResponse(setting.isPickupEnabled(), setting.isDineInEnabled(), setting.tablesOrEmpty(),
                    setting.getOpenTime(), setting.getCloseTime(), setting.getClosedDays() == null ? List.of() : setting.getClosedDays(),
                    setting.isPaused(), setting.getNotice(), openNow);
        }
    }
}

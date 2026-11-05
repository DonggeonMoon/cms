package com.dgmoonlabs.cms.domain.admin.ipaccess.dto;

import com.dgmoonlabs.cms.domain.admin.ipaccess.entity.AllowedIp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class AllowedIpDtos {
    private AllowedIpDtos() {
    }

    public record AllowedIpSaveRequest(@NotBlank @Size(max = 64) String address, @Size(max = 200) String memo) {
        public String memoOrEmpty() {
            return memo == null ? "" : memo.trim();
        }
    }

    public record EnabledRequest(@NotNull Boolean enabled) {
    }

    public record AllowedIpResponse(Long id, String address, String memo, LocalDateTime createdAt) {
        public static AllowedIpResponse from(final AllowedIp ip) {
            return new AllowedIpResponse(ip.getId(), ip.getAddress(), ip.getMemo(), ip.getCreatedAt());
        }
    }

    /** forceDisabled: 서버 설정으로 제한을 꺼 둔 상태(잠겼을 때 비상용). 켜져 있어도 검사하지 않는다. */
    public record AllowedIpSettings(boolean enabled, boolean forceDisabled, String currentIp, boolean currentIpAllowed,
                                    List<AllowedIpResponse> entries) {
    }
}

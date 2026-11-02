package com.dgmoonlabs.cms.domain.map.dto;

import com.dgmoonlabs.cms.domain.map.constant.MapProvider;
import com.dgmoonlabs.cms.domain.map.entity.Branch;
import com.dgmoonlabs.cms.domain.map.entity.MapSetting;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

public final class MapDtos {
    private MapDtos() {
    }

    @Builder
    public record MapSettingRequest(
            @NotNull MapProvider provider,
            @Size(max = 200) String kakaoAppKey,
            @Size(max = 200) String naverClientId,
            boolean directionsEnabled,
            boolean branchesEnabled,
            @Size(max = 100) String officeName,
            @Size(max = 255) String officeAddress,
            @Size(max = 255) String officeDetailAddress,
            @DecimalMin("-90") @DecimalMax("90") BigDecimal officeLatitude,
            @DecimalMin("-180") @DecimalMax("180") BigDecimal officeLongitude,
            @Size(max = 30) String officePhoneNumber,
            @Size(max = 200) String officeBusinessHours,
            @Size(max = 5000) String officeDescription
    ) {
        // "오시는 길" cannot be switched on without a place to point at
        public boolean hasOfficeLocation() {
            return officeAddress != null && !officeAddress.isBlank() && officeLatitude != null && officeLongitude != null;
        }
    }

    // admin view: every field, both keys
    public record MapSettingResponse(MapProvider provider, String kakaoAppKey, String naverClientId,
                                     boolean directionsEnabled, boolean branchesEnabled, Office office) {
        public static MapSettingResponse from(final MapSetting setting) {
            return new MapSettingResponse(setting.getProvider(), setting.getKakaoAppKey(), setting.getNaverClientId(),
                    setting.isDirectionsEnabled(), setting.isBranchesEnabled(), Office.from(setting));
        }
    }

    public record Office(String name, String address, String detailAddress, BigDecimal latitude, BigDecimal longitude,
                         String phoneNumber, String businessHours, String description) {
        public static Office from(final MapSetting setting) {
            return new Office(setting.getOfficeName(), setting.getOfficeAddress(), setting.getOfficeDetailAddress(),
                    setting.getOfficeLatitude(), setting.getOfficeLongitude(), setting.getOfficePhoneNumber(),
                    setting.getOfficeBusinessHours(), setting.getOfficeDescription());
        }
    }

    // public view: only the selected provider's (browser-side, domain-restricted) key and only the enabled tabs
    public record PublicMapResponse(MapProvider provider, String apiKey, Office directions, List<BranchResponse> branches) {
    }

    @Builder
    public record BranchSaveRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 255) String address,
            @Size(max = 255) String detailAddress,
            @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
            @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
            @Size(max = 30) String phoneNumber,
            @Size(max = 200) String businessHours,
            @Size(max = 500) String description,
            @PositiveOrZero int sequence,
            boolean isUsing
    ) {
    }

    public record BranchResponse(Long id, String name, String address, String detailAddress, BigDecimal latitude,
                                 BigDecimal longitude, String phoneNumber, String businessHours, String description,
                                 int sequence, boolean isUsing) {
        public static BranchResponse from(final Branch branch) {
            return new BranchResponse(branch.getId(), branch.getName(), branch.getAddress(), branch.getDetailAddress(),
                    branch.getLatitude(), branch.getLongitude(), branch.getPhoneNumber(), branch.getBusinessHours(),
                    branch.getDescription(), branch.getSequence(), branch.isUsing());
        }
    }
}

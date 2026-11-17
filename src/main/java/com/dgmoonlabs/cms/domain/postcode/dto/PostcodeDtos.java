package com.dgmoonlabs.cms.domain.postcode.dto;

import com.dgmoonlabs.cms.domain.postcode.constant.PostcodeProvider;
import com.dgmoonlabs.cms.domain.postcode.entity.PostcodeSetting;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

public final class PostcodeDtos {
    private PostcodeDtos() {
    }

    @Builder
    public record PostcodeSettingRequest(@NotNull PostcodeProvider provider, Long jusoApiKeyId) {
    }

    public record PostcodeSettingResponse(PostcodeProvider provider, Long jusoApiKeyId) {
        public static PostcodeSettingResponse from(final PostcodeSetting setting) {
            return new PostcodeSettingResponse(setting.getProvider(), setting.getJusoApiKeyId());
        }
    }

    public record PublicPostcodeResponse(PostcodeProvider provider) {
    }

    public record PostcodeAddress(String zonecode, String address, String roadAddress, String jibunAddress) {
    }

    public record PostcodeSearchResponse(List<PostcodeAddress> results, int totalCount) {
    }
}

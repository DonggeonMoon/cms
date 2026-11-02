package com.dgmoonlabs.cms.domain.admin.config.dto;

import com.dgmoonlabs.cms.domain.admin.config.constant.OptionType;
import com.dgmoonlabs.cms.domain.admin.config.entity.Config;

public record ConfigResponse(Long id, String key, String value, String description, OptionType type, boolean isHidden) {
    public static ConfigResponse from(final Config config) {
        return new ConfigResponse(config.getId(), config.getKey(), config.getValue(), config.getDescription(),
                config.getType(), config.isHidden());
    }
}

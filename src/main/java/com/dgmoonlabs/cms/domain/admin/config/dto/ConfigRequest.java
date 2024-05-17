package com.dgmoonlabs.cms.domain.admin.config.dto;

import com.dgmoonlabs.cms.domain.admin.config.constant.OptionType;
import com.dgmoonlabs.cms.domain.admin.config.entity.Config;
import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ConfigRequest {
    private Long id;
    private String key;
    private String value;
    private String description;
    private OptionType type;
    private boolean isHidden;

    public Config toEntity() {
        return Config.builder()
                .id(id)
                .key(key)
                .value(value)
                .description(description)
                .type(type)
                .isHidden(isHidden)
                .build();
    }
}

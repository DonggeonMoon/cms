package egovframework.com.cms.config.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigOptionUpdateDto extends ConfigOptionDto {
    @Builder
    public ConfigOptionUpdateDto(String confId, String optKey, String optValue, String optName, String optHelp, String optType, boolean optHidden, String optUnitText) {
        super(confId, optKey, optValue, optName, optHelp, optType, optHidden, optUnitText);
    }
}

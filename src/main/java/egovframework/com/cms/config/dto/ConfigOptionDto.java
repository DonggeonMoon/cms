package egovframework.com.cms.config.dto;

import egovframework.com.cms.config.model.ConfigOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public abstract class ConfigOptionDto {
    private String confId;
    private String optKey;
    private String optValue;
    private String optName;
    private String optHelp;
    private String optType;
    private boolean optHidden;
    private String optUnitText;

    public ConfigOption toEntity() {
        return ConfigOption.builder()
                .confId(confId)
                .optKey(optKey)
                .optValue(optValue)
                .optName(optName)
                .optHelp(optHelp)
                .optType(optType)
                .optHidden(optHidden)
                .optUnitText(optUnitText)
                .build();
    }
}

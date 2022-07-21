package egovframework.com.cms.config.model;

import egovframework.com.cms.config.dto.ConfigOptionCreateDto;
import egovframework.com.cms.config.dto.ConfigOptionResponseDto;
import egovframework.com.cms.config.dto.ConfigOptionUpdateDto;
import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "ECMS_CONFIG_OPTION")
@IdClass(ConfigOptionPK.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ConfigOption extends MultiSiteVO implements Serializable {
    @Id
    @WatchDog
    private String confId; // confId + optKey = unique
    @Id
    private String optKey; // confId + optKey = unique
    private String optValue;
    private String optName;
    private String optHelp;
    private String optType = "text"; // 옵션 유형 text, radio, checkbox, textarea
    private boolean optHidden = false;
    private String optUnitText;

    public static ConfigOption of(String confId, String optKey, String optValue, String optName, String optHelp, String optType, boolean optHidden, String optUnitText) {
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

    public void update(String confId, String optKey, String optValue, String optName, String optHelp, String optType, boolean optHidden, String optUnitText) {
        this.confId = confId;
        this.optKey = optKey;
        this.optValue = optValue;
        this.optName = optName;
        this.optHelp = optHelp;
        this.optType = optType;
        this.optHidden = optHidden;
        this.optUnitText = optUnitText;
    }

    public ConfigOptionCreateDto toCreateDto() {
        return ConfigOptionCreateDto.builder()
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

    public ConfigOptionResponseDto toResponseDto() {
        return ConfigOptionResponseDto.builder()
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

    public ConfigOptionUpdateDto toUpdateDto() {
        return ConfigOptionUpdateDto.builder()
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

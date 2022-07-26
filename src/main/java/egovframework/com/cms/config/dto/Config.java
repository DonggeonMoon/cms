package egovframework.com.cms.config.dto;

import egovframework.com.cms.config.model.ConfigOption;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Slf4j
public class Config {
    private String confId;
    private String sitePrefix;
    private List<ConfigOption> options;
    private Map<String, String> optionMap = new HashMap<String, String>();
    private MultipartFile watermarkImage;

    public Config(List<ConfigOption> options) {
        this.options = options;
    }

    public String getOption(String optKey) {
        for (ConfigOption configOption : options) {
            if (optKey.equals(configOption.getOptKey()) && configOption.getOptValue() == null) {
                log.info("Config option value for option key [{}] is null. Check option table.", optKey);
                return "";
            }
            if (optKey.equals(configOption.getOptKey()) && configOption.getOptValue() != null) {
                return configOption.getOptValue();
            }
        }
        return null;
    }

    public Map<String, String> getOptionMap() {
        if (!this.optionMap.isEmpty()) {
            return optionMap;
        }
        for (ConfigOption configOption : options) {
            if (StringUtils.isNotBlank(configOption.getOptKey())) {
                optionMap.put(configOption.getOptKey(), configOption.getOptValue());
            }
        }
        return optionMap;
    }

    public void updateOption(String key, String value) {
        for (ConfigOption configOption : options) {
            if (configOption.getOptKey().equals(key)) {
                configOption.toResponseDto().setOptValue(value);
                break;
            }
        }
    }
}

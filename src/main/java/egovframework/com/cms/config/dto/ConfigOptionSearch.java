package egovframework.com.cms.config.dto;

import egovframework.com.cms.config.model.ConfigOption;
import egovframework.com.cms.support.Pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Slf4j
public class ConfigOptionSearch extends PagingSearch {
    private String confId;
    private String sitePrefix;
    private List<ConfigOption> options;
    private Map<String, String> optionMap = new HashMap<String, String>();//jsp 뷰단에서 꺼내쓰려고 만든 변수임. 자바단에서는 쓸일 없음.
    private MultipartFile watermarkImage;

    public String getOption(String optKey) {
        for (ConfigOption configOption : options) {
            if (optKey.equals(configOption.getOptKey())) {
                if (configOption.getOptValue() == null) {
                    log.info("Config option value for option key [{}] is null, check option table.", optKey);
                    return "";
                } else {
                    return configOption.getOptValue();
                }
            }
        }
        return null;
    }
}

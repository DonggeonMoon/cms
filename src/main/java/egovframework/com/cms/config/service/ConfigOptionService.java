package egovframework.com.cms.config.service;

import egovframework.com.cms.config.dto.Config;
import egovframework.com.cms.config.dto.ConfigOptionCreateDto;
import egovframework.com.cms.config.dto.ConfigOptionSearch;
import egovframework.com.cms.config.dto.ConfigOptionUpdateDto;
import egovframework.com.cms.config.model.ConfigOption;

import java.util.List;

public interface ConfigOptionService {

    void saveConfigOption(ConfigOptionCreateDto configOptionCreateDto);

    ConfigOption findConfigOptionById(String id) throws Exception;

    List<ConfigOption> findList(ConfigOptionSearch configOptionSearch);

    void updateConfigOption(ConfigOptionUpdateDto configOptionUpdateDto) throws Exception;

    void deleteConfigOption(String id) throws Exception;

    Config getConfig(String confId);
}

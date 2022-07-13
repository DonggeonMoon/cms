package egovframework.com.cms.config.service;

import egovframework.com.cms.config.dto.ConfigOptionCreateDto;
import egovframework.com.cms.config.dto.ConfigOptionSearch;
import egovframework.com.cms.config.dto.ConfigOptionUpdateDto;
import egovframework.com.cms.config.model.ConfigOption;
import egovframework.com.cms.support.Pagination.Paging;

public interface ConfigOptionService {

    void saveConfigOption(ConfigOptionCreateDto configOptionCreateDto);

    ConfigOption findConfigOptionById(String id) throws Exception;

    Paging findPage(ConfigOptionSearch configOptionSearch);

    void updateConfigOption(ConfigOptionUpdateDto configOptionUpdateDto) throws Exception;

    void deleteConfigOption(String id) throws Exception;
}

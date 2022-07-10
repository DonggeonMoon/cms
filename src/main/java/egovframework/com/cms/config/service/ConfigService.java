package egovframework.com.cms.config.service;

import egovframework.com.cms.config.dto.Config;
import egovframework.com.cms.config.dto.ConfigCreateDto;
import egovframework.com.cms.config.dto.ConfigSearch;
import egovframework.com.cms.config.dto.ConfigUpdateDto;
import egovframework.com.cms.support.Pagination.Paging;

public interface ConfigService {

    void saveConfig(ConfigCreateDto configCreateDto);

    Config findConfigById(String id) throws Exception;

    Paging findPage(ConfigSearch configSearch);

    void updateConfig(ConfigUpdateDto configUpdateDto) throws Exception;

    void deleteConfig(String id) throws Exception;
}

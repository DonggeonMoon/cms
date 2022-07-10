package egovframework.com.cms.config.service.impl;

import egovframework.com.cms.config.dto.Config;
import egovframework.com.cms.config.dto.ConfigCreateDto;
import egovframework.com.cms.config.dto.ConfigSearch;
import egovframework.com.cms.config.dto.ConfigUpdateDto;
import egovframework.com.cms.config.repository.ConfigRepository;
import egovframework.com.cms.config.service.ConfigService;
import egovframework.com.cms.support.Pagination.Paging;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    private final ConfigRepository configRepository;

    @Override
    public void saveConfig(ConfigCreateDto configCreateDto) {

    }

    @Override
    public Config findConfigById(String id) throws Exception {
        return null;
    }

    @Override
    public Paging findPage(ConfigSearch configSearch) {
        return null;
    }

    @Override
    public void updateConfig(ConfigUpdateDto configUpdateDto) throws Exception {

    }

    @Override
    public void deleteConfig(String id) throws Exception {

    }
}

package egovframework.com.cms.config.service.impl;

import egovframework.com.cms.config.dto.ConfigOptionCreateDto;
import egovframework.com.cms.config.dto.ConfigOptionSearch;
import egovframework.com.cms.config.dto.ConfigOptionUpdateDto;
import egovframework.com.cms.config.model.ConfigOption;
import egovframework.com.cms.config.repository.ConfigOptionRepository;
import egovframework.com.cms.config.service.ConfigOptionService;
import egovframework.com.cms.support.Pagination.Paging;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigOptionServiceImpl implements ConfigOptionService {
    private final ConfigOptionRepository configRepository;

    @Override
    public void saveConfigOption(ConfigOptionCreateDto configCreateDto) {

    }

    @Override
    public ConfigOption findConfigOptionById(String id) throws Exception {
        return null;
    }

    @Override
    public Paging findPage(ConfigOptionSearch configSearch) {
        return null;
    }

    @Override
    public void updateConfigOption(ConfigOptionUpdateDto configUpdateDto) throws Exception {

    }

    @Override
    public void deleteConfigOption(String id) throws Exception {

    }
}

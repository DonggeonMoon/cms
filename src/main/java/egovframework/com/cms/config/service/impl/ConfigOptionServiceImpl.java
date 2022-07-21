package egovframework.com.cms.config.service.impl;

import egovframework.com.cms.config.dto.Config;
import egovframework.com.cms.config.dto.ConfigOptionCreateDto;
import egovframework.com.cms.config.dto.ConfigOptionSearch;
import egovframework.com.cms.config.dto.ConfigOptionUpdateDto;
import egovframework.com.cms.config.model.ConfigOption;
import egovframework.com.cms.config.repository.ConfigOptionRepository;
import egovframework.com.cms.config.service.ConfigOptionService;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.support.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigOptionServiceImpl implements ConfigOptionService {
    private final ConfigOptionRepository configOptionRepository;

    @Override
    public void saveConfigOption(ConfigOptionCreateDto configCreateDto) {
        configOptionRepository.save(configCreateDto.toEntity());
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigOption findConfigOptionById(String id) throws Exception {
        return configOptionRepository.findById(id).orElseThrow(() -> new Exception());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigOption> findList(ConfigOptionSearch configOptionSearch) {
        return configOptionRepository.findList(configOptionSearch);
    }

    @Override
    public void updateConfigOption(ConfigOptionUpdateDto configOptionUpdateDto) throws Exception {
        ConfigOption configOption = configOptionRepository.findById(configOptionUpdateDto.getConfId()).orElseThrow(() -> new Exception());
        configOption.update(configOptionUpdateDto.getConfId(), configOptionUpdateDto.getOptKey(), configOptionUpdateDto.getOptValue(),
                configOptionUpdateDto.getOptName(), configOptionUpdateDto.getOptHelp(), configOptionUpdateDto.getOptType(),
                configOptionUpdateDto.isOptHidden(), configOptionUpdateDto.getOptUnitText());
    }

    @Override
    public void deleteConfigOption(String id) throws Exception {
        ConfigOption configOption = configOptionRepository.findById(id).orElseThrow(() -> new Exception());
        configOptionRepository.delete(configOption);
    }

    @Override
    public Config getConfig(String confId) {
        String sitePrefix = null;
        //사이트별 설정 대신 전역설정을 사용할 옵션 구분
        if ("global".equals(confId) || "mail".equals(confId) || "sns".equals(confId) || "comment".equals(confId) || "watermark".equals(confId)) {
            //siteId = Constant.MAIN_SITE_ID;
            sitePrefix = Constant.MAIN_SITE_PREFIX;
        } else {
            Site currentSite;

            try {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                currentSite = (Site) request.getAttribute("currentSite");
            } catch (IllegalStateException e) {
                currentSite = new Site();
                currentSite.setSiteId(Constant.MAIN_SITE_DISPLAY_ID);
            }
            sitePrefix = currentSite.getSitePrefix();
        }

        ConfigOptionSearch configOptionSearch = new ConfigOptionSearch();
        configOptionSearch.setConfId(confId);
        return new Config(configOptionRepository.findList(configOptionSearch));
    }

}

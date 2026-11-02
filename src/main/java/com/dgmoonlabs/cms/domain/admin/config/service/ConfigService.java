package com.dgmoonlabs.cms.domain.admin.config.service;

import com.dgmoonlabs.cms.domain.admin.config.dto.ConfigResponse;
import com.dgmoonlabs.cms.domain.admin.config.dto.ConfigSaveRequest;
import com.dgmoonlabs.cms.domain.admin.config.entity.Config;
import com.dgmoonlabs.cms.domain.admin.config.repository.ConfigRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class ConfigService {
    private final ConfigRepository configRepository;

    @Transactional(readOnly = true)
    public Page<ConfigResponse> searchConfigs(final String keyword, final Pageable pageable) {
        return configRepository.findAllByDeletedAtIsNullAndKeyContainingIgnoreCaseOrderByKeyAsc(
                keyword == null ? "" : keyword, pageable).map(ConfigResponse::from);
    }

    @Transactional(readOnly = true)
    public ConfigResponse getConfig(final Long id) {
        return ConfigResponse.from(findConfig(id));
    }

    @Transactional(readOnly = true)
    public Map<String, String> getPublicConfigs() {
        Map<String, String> configs = new TreeMap<>();
        configRepository.findAllByIsHiddenFalseAndDeletedAtIsNull()
                .forEach(config -> configs.put(config.getKey(), config.getValue()));
        return configs;
    }

    @Transactional
    public Long createConfig(final ConfigSaveRequest request) {
        if (configRepository.existsByKeyAndDeletedAtIsNull(request.key())) {
            throw new BusinessException(ErrorCode.DUPLICATE_CONFIG_KEY);
        }
        return configRepository.save(request.toEntity()).getId();
    }

    @Transactional
    public void updateConfig(final Long id, final ConfigSaveRequest request) {
        findConfig(id).update(request);
    }

    @Transactional
    public void deleteConfig(final Long id) {
        findConfig(id).delete();
    }

    private Config findConfig(final Long id) {
        return configRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }
}

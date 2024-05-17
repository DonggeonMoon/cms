package com.dgmoonlabs.cms.domain.admin.config.service;

import com.dgmoonlabs.cms.domain.admin.config.dto.ConfigRequest;
import com.dgmoonlabs.cms.domain.admin.config.entity.Config;
import com.dgmoonlabs.cms.domain.admin.config.repository.ConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigService {
    private final ConfigRepository configRepository;

    public Page<Config> getMultiple(final Pageable pageable) {
        return configRepository.findAll(pageable);
    }

    public Config getOne(final Long id) {
        return configRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    public Long save(final ConfigRequest request) {
        return configRepository.save(request.toEntity())
                .getId();
    }

    public void delete(final Long id) {
        configRepository.deleteById(id);
    }
}

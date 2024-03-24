package com.dgmoonlabs.cms.domain.admin.config.repository;

import com.dgmoonlabs.cms.domain.admin.config.dto.ConfigRequest;
import com.dgmoonlabs.cms.domain.admin.config.entity.Config;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ConfigCustomRepository {
    List<Config> find(ConfigRequest request, Pageable pageable);

    List<Config> find(ConfigRequest siteRequest);
}

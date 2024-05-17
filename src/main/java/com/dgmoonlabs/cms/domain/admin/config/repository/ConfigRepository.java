package com.dgmoonlabs.cms.domain.admin.config.repository;

import com.dgmoonlabs.cms.domain.admin.config.entity.Config;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<Config, Long>, ConfigCustomRepository {
}

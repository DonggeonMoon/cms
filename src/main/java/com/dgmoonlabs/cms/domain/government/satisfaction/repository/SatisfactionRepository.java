package com.dgmoonlabs.cms.domain.government.satisfaction.repository;

import com.dgmoonlabs.cms.domain.government.satisfaction.entity.Satisfaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface SatisfactionRepository extends JpaRepository<Satisfaction, Long>, SatisfactionCustomRepository {
    Optional<Satisfaction> findByMenuIdAndUserId(Long menuId, Long userId);
}
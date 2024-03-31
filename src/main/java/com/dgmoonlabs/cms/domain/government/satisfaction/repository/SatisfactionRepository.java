package com.dgmoonlabs.cms.domain.government.satisfaction.repository;

import com.dgmoonlabs.cms.domain.government.satisfaction.entity.Satisfaction;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SatisfactionRepository extends JpaRepository<Satisfaction, Long>, SatisfactionCustomRepository {
}
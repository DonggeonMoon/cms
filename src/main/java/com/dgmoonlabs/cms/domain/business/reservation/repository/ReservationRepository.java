package com.dgmoonlabs.cms.domain.business.reservation.repository;

import com.dgmoonlabs.cms.domain.admin.statistics.entity.Statistics;
import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Statistics, Long>, StatisticsCustomRepository {
}

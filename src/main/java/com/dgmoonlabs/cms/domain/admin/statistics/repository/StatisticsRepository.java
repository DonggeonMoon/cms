package com.dgmoonlabs.cms.domain.admin.statistics.repository;

import com.dgmoonlabs.cms.domain.admin.statistics.entity.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    Optional<Statistics> findFirstByDateAndNationCodeAndOsAndBrowserAndUrl(LocalDate date, String nationCode, String os,
                                                                          String browser, String url);
}

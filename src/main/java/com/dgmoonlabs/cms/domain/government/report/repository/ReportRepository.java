package com.dgmoonlabs.cms.domain.government.report.repository;

import com.dgmoonlabs.cms.domain.government.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}

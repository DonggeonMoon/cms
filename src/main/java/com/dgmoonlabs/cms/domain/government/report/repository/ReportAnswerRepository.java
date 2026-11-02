package com.dgmoonlabs.cms.domain.government.report.repository;

import com.dgmoonlabs.cms.domain.government.report.entity.answer.ReportAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportAnswerRepository extends JpaRepository<ReportAnswer, Long> {
    List<ReportAnswer> findAllByReportIdAndDeletedAtIsNullOrderByIdAsc(Long reportId);

    Optional<ReportAnswer> findByIdAndReportIdAndDeletedAtIsNull(Long id, Long reportId);

    int countByReportIdAndDeletedAtIsNull(Long reportId);
}

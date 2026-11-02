package com.dgmoonlabs.cms.domain.government.report.repository;

import com.dgmoonlabs.cms.domain.government.report.constant.ReportStatus;
import com.dgmoonlabs.cms.domain.government.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByIdAndDeletedAtIsNull(Long id);

    Page<Report> findAllByUserIdAndDeletedAtIsNullOrderByIdDesc(Long userId, Pageable pageable);

    @Query("select r from Report r where r.deletedAt is null and (:status is null or r.status = :status) "
            + "and (:keyword = '' or lower(r.title) like lower(concat('%', :keyword, '%'))) order by r.id desc")
    Page<Report> search(ReportStatus status, String keyword, Pageable pageable);

    long countByStatusAndDeletedAtIsNull(ReportStatus status);
}

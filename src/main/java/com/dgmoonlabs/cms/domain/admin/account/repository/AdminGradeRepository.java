package com.dgmoonlabs.cms.domain.admin.account.repository;

import com.dgmoonlabs.cms.domain.admin.account.entity.AdminGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminGradeRepository extends JpaRepository<AdminGrade, Long> {
    List<AdminGrade> findAllByDeletedAtIsNullOrderBySuperAdminDescIdAsc();

    Optional<AdminGrade> findByIdAndDeletedAtIsNull(Long id);

    Optional<AdminGrade> findFirstBySuperAdminTrueAndDeletedAtIsNull();
}

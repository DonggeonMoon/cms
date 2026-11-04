package com.dgmoonlabs.cms.domain.application.repository;

import com.dgmoonlabs.cms.domain.application.entity.ApplicationForm;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ApplicationFormRepository extends JpaRepository<ApplicationForm, Long> {
    Optional<ApplicationForm> findByIdAndDeletedAtIsNull(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from ApplicationForm f where f.id = :id and f.deletedAt is null")
    Optional<ApplicationForm> findForUpdate(Long id);

    Page<ApplicationForm> findAllByDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdDesc(String keyword, Pageable pageable);

    Page<ApplicationForm> findAllByIsEnabledTrueAndDeletedAtIsNullAndTitleContainingIgnoreCaseOrderByIdDesc(String keyword, Pageable pageable);
}

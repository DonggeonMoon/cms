package com.dgmoonlabs.cms.domain.business.reservation.repository;

import com.dgmoonlabs.cms.domain.business.reservation.entity.program.Program;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {
    Optional<Program> findByIdAndDeletedAtIsNull(Long id);

    List<Program> findAllByIsEnabledTrueAndDeletedAtIsNullOrderByIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Program p where p.id = :id")
    Optional<Program> findForUpdate(Long id);

    Page<Program> findAllByDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(String keyword, Pageable pageable);

    Page<Program> findAllByIsEnabledTrueAndDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(String keyword,
                                                                                                  Pageable pageable);
}

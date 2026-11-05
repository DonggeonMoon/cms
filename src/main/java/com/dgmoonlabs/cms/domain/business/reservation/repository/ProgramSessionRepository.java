package com.dgmoonlabs.cms.domain.business.reservation.repository;

import com.dgmoonlabs.cms.domain.business.reservation.entity.program.ProgramSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProgramSessionRepository extends JpaRepository<ProgramSession, Long> {
    List<ProgramSession> findAllByProgramIdAndDeletedAtIsNullOrderByStartAtAsc(Long programId);

    boolean existsByProgramIdAndDeletedAtIsNull(Long programId);

    Optional<ProgramSession> findByIdAndProgramIdAndDeletedAtIsNull(Long id, Long programId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ProgramSession s where s.id = :id")
    Optional<ProgramSession> findForUpdate(Long id);
}

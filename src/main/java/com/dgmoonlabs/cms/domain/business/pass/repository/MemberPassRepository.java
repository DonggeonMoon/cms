package com.dgmoonlabs.cms.domain.business.pass.repository;

import com.dgmoonlabs.cms.domain.business.pass.constant.PassStatus;
import com.dgmoonlabs.cms.domain.business.pass.entity.MemberPass;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberPassRepository extends JpaRepository<MemberPass, Long> {
    boolean existsByFirstOrderId(Long orderId);

    Optional<MemberPass> findByIdAndMemberId(Long id, Long memberId);

    List<MemberPass> findAllByMemberIdAndProgramIdAndStatusOrderByIdAsc(Long memberId, Long programId, PassStatus status);

    Page<MemberPass> findAllByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    @Query("select p from MemberPass p where (:memberId is null or p.memberId = :memberId) order by p.id desc")
    Page<MemberPass> search(Long memberId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from MemberPass p where p.id = :id")
    Optional<MemberPass> findForUpdate(Long id);
}

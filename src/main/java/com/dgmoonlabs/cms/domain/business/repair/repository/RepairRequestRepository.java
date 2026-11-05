package com.dgmoonlabs.cms.domain.business.repair.repository;

import com.dgmoonlabs.cms.domain.business.repair.entity.RepairRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {
    Page<RepairRequest> findAllByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);

    Optional<RepairRequest> findByIdAndMemberId(Long id, Long memberId);

    @Query("select r from RepairRequest r where :keyword = '' or lower(r.title) like lower(concat('%', :keyword, '%')) order by r.id desc")
    Page<RepairRequest> search(String keyword, Pageable pageable);
}

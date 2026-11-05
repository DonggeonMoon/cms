package com.dgmoonlabs.cms.domain.business.accounting.repository;

import com.dgmoonlabs.cms.domain.business.accounting.entity.Receivable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceivableRepository extends JpaRepository<Receivable, Long> {
    Page<Receivable> findAllByOrderByIdDesc(Pageable pageable);

    Page<Receivable> findAllByMemberIdOrderByIdDesc(Long memberId, Pageable pageable);
}

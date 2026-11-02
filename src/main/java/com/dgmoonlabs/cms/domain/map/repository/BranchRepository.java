package com.dgmoonlabs.cms.domain.map.repository;

import com.dgmoonlabs.cms.domain.map.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByIdAndDeletedAtIsNull(Long id);

    Page<Branch> findAllByDeletedAtIsNullAndNameContainingIgnoreCaseOrderBySequenceAscIdAsc(String keyword, Pageable pageable);

    List<Branch> findAllByIsUsingTrueAndDeletedAtIsNullOrderBySequenceAscIdAsc();
}

package com.dgmoonlabs.cms.domain.business.stock.repository;

import com.dgmoonlabs.cms.domain.business.stock.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    Page<StockMovement> findAllByProductIdOrderByIdDesc(Long productId, Pageable pageable);
}

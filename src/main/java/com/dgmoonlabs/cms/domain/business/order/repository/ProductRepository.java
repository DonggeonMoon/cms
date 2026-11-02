package com.dgmoonlabs.cms.domain.business.order.repository;

import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndDeletedAtIsNull(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findForUpdate(Long id);

    Page<Product> findAllByDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(String keyword, Pageable pageable);

    Page<Product> findAllByIsEnabledTrueAndDeletedAtIsNullAndNameContainingIgnoreCaseOrderByIdDesc(String keyword,
                                                                                                  Pageable pageable);
}

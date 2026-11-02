package com.dgmoonlabs.cms.domain.business.order.repository;

import com.dgmoonlabs.cms.domain.business.order.entity.product.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    List<ProductOption> findAllByProductIdAndDeletedAtIsNullOrderByIdAsc(Long productId);

    Optional<ProductOption> findByIdAndProductIdAndDeletedAtIsNull(Long id, Long productId);
}

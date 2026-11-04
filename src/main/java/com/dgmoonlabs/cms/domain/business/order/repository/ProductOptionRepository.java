package com.dgmoonlabs.cms.domain.business.order.repository;

import com.dgmoonlabs.cms.domain.business.order.entity.product.ProductOption;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
    List<ProductOption> findAllByProductIdAndDeletedAtIsNullOrderByIdAsc(Long productId);

    Optional<ProductOption> findByIdAndProductIdAndDeletedAtIsNull(Long id, Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from ProductOption o where o.id = :id")
    Optional<ProductOption> findForUpdate(Long id);

    List<ProductOption> findAllByIdIn(Collection<Long> ids);
}

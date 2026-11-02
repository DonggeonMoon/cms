package com.dgmoonlabs.cms.domain.business.discount.repository;

import com.dgmoonlabs.cms.domain.business.discount.entity.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Optional<Discount> findByIdAndDeletedAtIsNull(Long id);

    Page<Discount> findAllByDeletedAtIsNullOrderByIdDesc(Pageable pageable);

    // discounts without a product apply to every product
    @Query("select d from Discount d where d.deletedAt is null and d.isUsing = true "
            + "and (d.productId is null or d.productId = :productId) order by d.rate desc")
    List<Discount> findApplicable(Long productId);
}

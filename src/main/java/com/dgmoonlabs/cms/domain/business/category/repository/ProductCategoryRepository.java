package com.dgmoonlabs.cms.domain.business.category.repository;

import com.dgmoonlabs.cms.domain.business.category.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    List<ProductCategory> findAllByDeletedAtIsNullOrderBySequenceAscIdAsc();

    Optional<ProductCategory> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByParentIdAndDeletedAtIsNull(Long parentId);
}

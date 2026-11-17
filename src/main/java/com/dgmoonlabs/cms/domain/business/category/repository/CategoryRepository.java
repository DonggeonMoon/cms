package com.dgmoonlabs.cms.domain.business.category.repository;

import com.dgmoonlabs.cms.domain.business.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByDeletedAtIsNullOrderBySequenceAscIdAsc();

    Optional<Category> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByParentIdAndDeletedAtIsNull(Long parentId);
}

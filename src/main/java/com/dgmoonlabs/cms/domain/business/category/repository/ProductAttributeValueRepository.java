package com.dgmoonlabs.cms.domain.business.category.repository;

import com.dgmoonlabs.cms.domain.business.category.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {
    List<ProductAttributeValue> findAllByProductId(Long productId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ProductAttributeValue v where v.productId = :productId")
    void deleteAllByProductId(Long productId);
}

package com.dgmoonlabs.cms.domain.business.order.repository;

import com.dgmoonlabs.cms.domain.business.order.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findAllByMemberIdOrderByIdAsc(Long memberId);

    List<CartItem> findAllByMemberIdAndIdIn(Long memberId, Collection<Long> ids);

    Optional<CartItem> findByIdAndMemberId(Long id, Long memberId);

    Optional<CartItem> findByMemberIdAndProductIdAndProductOptionId(Long memberId, Long productId, Long productOptionId);

    Optional<CartItem> findByMemberIdAndProductIdAndProductOptionIdIsNull(Long memberId, Long productId);

    List<CartItem> findAllByRemindedAtIsNullAndCreatedAtLessThanEqual(LocalDateTime threshold);
}

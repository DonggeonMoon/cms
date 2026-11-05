package com.dgmoonlabs.cms.domain.business.wishlist.repository;

import com.dgmoonlabs.cms.domain.business.wishlist.entity.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    Optional<WishlistItem> findByMemberIdAndProductId(Long memberId, Long productId);

    @Query("select w from WishlistItem w where w.memberId = :memberId and exists "
            + "(select 1 from Product p where p.id = w.productId and p.deletedAt is null) order by w.id desc")
    Page<WishlistItem> findLiveByMemberId(Long memberId, Pageable pageable);

    @Query("select w.productId from WishlistItem w where w.memberId = :memberId and exists "
            + "(select 1 from Product p where p.id = w.productId and p.deletedAt is null)")
    List<Long> findProductIdsByMemberId(Long memberId);

    long countByProductId(Long productId);
}

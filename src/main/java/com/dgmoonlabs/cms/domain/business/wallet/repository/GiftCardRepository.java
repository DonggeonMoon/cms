package com.dgmoonlabs.cms.domain.business.wallet.repository;

import com.dgmoonlabs.cms.domain.business.wallet.entity.GiftCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GiftCardRepository extends JpaRepository<GiftCard, Long> {
    boolean existsByCode(String code);

    Optional<GiftCard> findByCode(String code);

    Page<GiftCard> findAllByIssuedMemberIdOrderByIdDesc(Long issuedMemberId, Pageable pageable);
}

package com.dgmoonlabs.cms.domain.common.social.repository;

import com.dgmoonlabs.cms.domain.common.social.constant.SocialProvider;
import com.dgmoonlabs.cms.domain.common.social.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    List<SocialAccount> findAllByMemberIdOrderByIdAsc(Long memberId);
}

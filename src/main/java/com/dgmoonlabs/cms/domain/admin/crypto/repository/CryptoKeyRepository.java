package com.dgmoonlabs.cms.domain.admin.crypto.repository;

import com.dgmoonlabs.cms.domain.admin.crypto.entity.CryptoKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CryptoKeyRepository extends JpaRepository<CryptoKey, Long> {
    Optional<CryptoKey> findFirstByOrderByIdAsc();
}

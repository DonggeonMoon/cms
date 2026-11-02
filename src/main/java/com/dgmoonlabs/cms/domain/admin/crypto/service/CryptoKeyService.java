package com.dgmoonlabs.cms.domain.admin.crypto.service;

import com.dgmoonlabs.cms.domain.admin.crypto.dto.CryptoKeyDtos.CryptoKeyResponse;
import com.dgmoonlabs.cms.domain.admin.crypto.dto.CryptoKeyDtos.ReencryptResponse;
import com.dgmoonlabs.cms.domain.admin.crypto.entity.CryptoKey;
import com.dgmoonlabs.cms.domain.admin.crypto.repository.CryptoKeyRepository;
import com.dgmoonlabs.cms.global.crypto.CryptoAlgorithm;
import com.dgmoonlabs.cms.global.crypto.DataKeys;
import com.dgmoonlabs.cms.global.crypto.EncryptedColumns;
import com.dgmoonlabs.cms.global.crypto.MasterKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoKeyService {
    private static final CryptoAlgorithm DEFAULT_ALGORITHM = CryptoAlgorithm.AES;

    private final CryptoKeyRepository cryptoKeyRepository;
    private final MasterKey masterKey;
    private final EncryptedColumns encryptedColumns;
    private final TransactionTemplate transactionTemplate;
    private final PlatformTransactionManager transactionManager;

    @EventListener(ApplicationReadyEvent.class)
    public synchronized void loadOnStartup() {
        encryptedColumns.widen();
        CryptoKey key = cryptoKeyRepository.findFirstByOrderByIdAsc().orElse(null);
        if (key == null) {
            DataKeys.clear();
            return;
        }
        install(key);
        int changed = reencrypt();
        if (changed > 0) {
            log.info("시작 시 암호화되지 않았거나 이전 키로 남아 있던 값 {}건을 다시 암호화했습니다.", changed);
        }
    }

    public synchronized void initializeIfMissing() {
        if (cryptoKeyRepository.findFirstByOrderByIdAsc().isPresent()) {
            return;
        }
        CryptoKey key = ownTransaction().execute(status -> cryptoKeyRepository.save(CryptoKey.builder()
                .algorithm(DEFAULT_ALGORITHM)
                .wrappedKey(masterKey.wrap(UUID.randomUUID().toString()))
                .build()));
        install(key);
        log.info("암호화 키를 새로 만들고 기존 사용자 데이터 {}건을 암호화했습니다.", reencrypt());
    }

    public CryptoKeyResponse get() {
        return cryptoKeyRepository.findFirstByOrderByIdAsc().map(this::toResponse).orElseGet(CryptoKeyResponse::none);
    }

    public synchronized ReencryptResponse regenerate() {
        if (cryptoKeyRepository.findFirstByOrderByIdAsc().isEmpty()) {
            initializeIfMissing();
            return new ReencryptResponse(get(), 0);
        }
        CryptoKey key = ownTransaction().execute(status -> {
            CryptoKey current = cryptoKeyRepository.findFirstByOrderByIdAsc().orElseThrow();
            current.replaceKey(masterKey.wrap(UUID.randomUUID().toString()));
            return cryptoKeyRepository.saveAndFlush(current);
        });
        install(key);
        int changed = reencrypt();
        return new ReencryptResponse(toResponse(key), changed);
    }

    public synchronized ReencryptResponse changeAlgorithm(final CryptoAlgorithm algorithm) {
        initializeIfMissing();
        CryptoKey key = ownTransaction().execute(status -> {
            CryptoKey current = cryptoKeyRepository.findFirstByOrderByIdAsc().orElseThrow();
            current.changeAlgorithm(algorithm);
            return cryptoKeyRepository.saveAndFlush(current);
        });
        install(key);
        int changed = reencrypt();
        return new ReencryptResponse(toResponse(key), changed);
    }

    private int reencrypt() {
        Integer changed = transactionTemplate.execute(status -> encryptedColumns.reencrypt());
        return changed == null ? 0 : changed;
    }

    private TransactionTemplate ownTransaction() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }

    private void install(final CryptoKey key) {
        List<UUID> retired = key.getRetiredWrappedKeyList().stream().map(wrapped -> UUID.fromString(masterKey.unwrap(wrapped))).toList();
        DataKeys.install(key.getAlgorithm(), UUID.fromString(masterKey.unwrap(key.getWrappedKey())), retired);
    }

    private CryptoKeyResponse toResponse(final CryptoKey key) {
        return new CryptoKeyResponse(true, key.getAlgorithm(), masterKey.unwrap(key.getWrappedKey()), key.getCreatedAt(),
                key.getModifiedAt());
    }
}

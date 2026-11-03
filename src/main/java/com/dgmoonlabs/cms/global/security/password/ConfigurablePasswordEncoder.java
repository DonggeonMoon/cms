package com.dgmoonlabs.cms.global.security.password;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ConfigurablePasswordEncoder implements PasswordEncoder {
    private static final Map<PasswordHashAlgorithm, PasswordEncoder> ENCODERS = new EnumMap<>(Map.of(
            PasswordHashAlgorithm.BCRYPT, new BCryptPasswordEncoder(),
            PasswordHashAlgorithm.ARGON2, Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8(),
            PasswordHashAlgorithm.SCRYPT, SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8(),
            PasswordHashAlgorithm.PBKDF2, Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()));

    private static final Map<PasswordHashAlgorithm, DelegatingPasswordEncoder> BY_DEFAULT = new EnumMap<>(PasswordHashAlgorithm.class);

    static {
        Map<String, PasswordEncoder> byId = new java.util.HashMap<>();
        ENCODERS.forEach((algorithm, encoder) -> byId.put(algorithm.getId(), encoder));
        for (PasswordHashAlgorithm algorithm : PasswordHashAlgorithm.values()) {
            BY_DEFAULT.put(algorithm, new DelegatingPasswordEncoder(algorithm.getId(), byId));
        }
    }

    // 예전에 만든 해시도 확인할 수 있게 Spring Security 가 아는 모든 id 로 검증한다.
    private final PasswordEncoder verifier = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final PasswordHashSettingRepository repository;
    private volatile PasswordHashAlgorithm current;

    @Override
    public String encode(final CharSequence rawPassword) {
        return BY_DEFAULT.get(current()).encode(rawPassword);
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        return encodedPassword != null && verifier.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(final String encodedPassword) {
        return encodedPassword != null && !encodedPassword.startsWith("{" + current().getId() + "}");
    }

    @Transactional(readOnly = true)
    public PasswordHashAlgorithm current() {
        PasswordHashAlgorithm algorithm = current;
        if (algorithm == null) {
            algorithm = repository.findFirstByOrderByIdAsc().map(PasswordHashSetting::getAlgorithm).orElse(PasswordHashAlgorithm.BCRYPT);
            current = algorithm;
        }
        return algorithm;
    }

    @Transactional
    public void change(final PasswordHashAlgorithm algorithm) {
        repository.findFirstByOrderByIdAsc()
                .orElseGet(() -> repository.save(PasswordHashSetting.builder().build()))
                .change(algorithm);
        current = algorithm;
        // 롤백된 변경이 메모리에 남지 않게 트랜잭션이 끝나면 DB 에서 다시 읽는다.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(final int status) {
                    current = null;
                }
            });
        }
    }
}

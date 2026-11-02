package com.dgmoonlabs.cms.global.crypto;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataCipherTest {
    @ParameterizedTest
    @EnumSource(CryptoAlgorithm.class)
    void sealsAndOpensWithEachAlgorithm(final CryptoAlgorithm algorithm) {
        byte[] key = DataCipher.keyBytes(UUID.randomUUID());
        String sealed = DataCipher.encrypt("홍길동 010-1234-5678", algorithm, key);

        assertThat(sealed).startsWith("ENC:" + algorithm.name() + ":" + DataCipher.keyId(key) + ":");
        assertThat(DataCipher.encrypt("홍길동 010-1234-5678", algorithm, key)).isNotEqualTo(sealed);
        assertThat(DataCipher.decrypt(DataCipher.header(sealed), key)).isEqualTo("홍길동 010-1234-5678");
    }

    @ParameterizedTest
    @EnumSource(CryptoAlgorithm.class)
    void refusesAnotherKey(final CryptoAlgorithm algorithm) {
        String sealed = DataCipher.encrypt("비밀", algorithm, DataCipher.keyBytes(UUID.randomUUID()));

        assertThatThrownBy(() -> DataCipher.decrypt(DataCipher.header(sealed), DataCipher.keyBytes(UUID.randomUUID())))
                .isInstanceOf(IllegalStateException.class);
    }
}

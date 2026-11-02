package com.dgmoonlabs.cms.domain.admin.crypto.dto;

import com.dgmoonlabs.cms.global.crypto.CryptoAlgorithm;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class CryptoKeyDtos {
    private CryptoKeyDtos() {
    }

    public record CryptoKeyResponse(boolean initialized, CryptoAlgorithm algorithm, String key, LocalDateTime createdAt,
                                    LocalDateTime modifiedAt) {
        public static CryptoKeyResponse none() {
            return new CryptoKeyResponse(false, null, null, null, null);
        }
    }

    public record CryptoAlgorithmRequest(@NotNull CryptoAlgorithm algorithm) {
    }

    public record ReencryptResponse(CryptoKeyResponse key, int reencryptedValues) {
    }
}

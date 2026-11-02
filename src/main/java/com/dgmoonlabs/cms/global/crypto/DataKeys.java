package com.dgmoonlabs.cms.global.crypto;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DataKeys {
    private static volatile Snapshot snapshot = Snapshot.EMPTY;

    private DataKeys() {
    }

    public static void install(final CryptoAlgorithm algorithm, final UUID current, final Collection<UUID> retired) {
        Map<String, byte[]> keys = new HashMap<>();
        for (UUID old : retired) {
            byte[] bytes = DataCipher.keyBytes(old);
            keys.put(DataCipher.keyId(bytes), bytes);
        }
        byte[] currentBytes = DataCipher.keyBytes(current);
        String currentId = DataCipher.keyId(currentBytes);
        keys.put(currentId, currentBytes);
        snapshot = new Snapshot(algorithm, currentId, Map.copyOf(keys));
    }

    public static void clear() {
        snapshot = Snapshot.EMPTY;
    }

    public static String protect(final String plain) {
        Snapshot current = snapshot;
        if (plain == null || current.algorithm() == null) {
            return plain;
        }
        return DataCipher.encrypt(plain, current.algorithm(), current.keys().get(current.keyId()));
    }

    public static String reveal(final String stored) {
        if (!DataCipher.isEncrypted(stored)) {
            return stored;
        }
        DataCipher.Header header = DataCipher.header(stored);
        byte[] key = snapshot.keys().get(header.keyId());
        if (key == null) {
            throw new IllegalStateException("암호화 키(" + header.keyId() + ")를 찾을 수 없습니다.");
        }
        return DataCipher.decrypt(header, key);
    }

    public static boolean isCurrent(final String stored) {
        Snapshot current = snapshot;
        if (stored == null || current.algorithm() == null) {
            return true;
        }
        if (!DataCipher.isEncrypted(stored)) {
            return false;
        }
        DataCipher.Header header = DataCipher.header(stored);
        return header.algorithm() == current.algorithm() && header.keyId().equals(current.keyId());
    }

    private record Snapshot(CryptoAlgorithm algorithm, String keyId, Map<String, byte[]> keys) {
        static final Snapshot EMPTY = new Snapshot(null, null, Map.of());
    }
}

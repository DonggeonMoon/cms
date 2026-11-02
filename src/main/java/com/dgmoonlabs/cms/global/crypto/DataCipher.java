package com.dgmoonlabs.cms.global.crypto;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.modes.GCMModeCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

public final class DataCipher {
    public static final String PREFIX = "ENC:";
    private static final int NONCE_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private DataCipher() {
    }

    public static byte[] keyBytes(final UUID key) {
        return ByteBuffer.allocate(16).putLong(key.getMostSignificantBits()).putLong(key.getLeastSignificantBits()).array();
    }

    public static String keyId(final byte[] key) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(key), 0, 4);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean isEncrypted(final String value) {
        return value != null && value.startsWith(PREFIX);
    }

    public static String encrypt(final String plain, final CryptoAlgorithm algorithm, final byte[] key) {
        byte[] nonce = new byte[NONCE_BYTES];
        RANDOM.nextBytes(nonce);
        byte[] sealed = run(true, algorithm, key, nonce, plain.getBytes(StandardCharsets.UTF_8));
        byte[] out = ByteBuffer.allocate(nonce.length + sealed.length).put(nonce).put(sealed).array();
        return PREFIX + algorithm.name() + ":" + keyId(key) + ":" + Base64.getEncoder().encodeToString(out);
    }

    public static Header header(final String stored) {
        String[] parts = stored.substring(PREFIX.length()).split(":", 3);
        if (parts.length != 3) {
            throw new IllegalStateException("암호문 형식이 올바르지 않습니다.");
        }
        return new Header(CryptoAlgorithm.valueOf(parts[0]), parts[1], parts[2]);
    }

    public static String decrypt(final Header header, final byte[] key) {
        byte[] in = Base64.getDecoder().decode(header.body());
        byte[] nonce = Arrays.copyOfRange(in, 0, NONCE_BYTES);
        byte[] sealed = Arrays.copyOfRange(in, NONCE_BYTES, in.length);
        return new String(run(false, header.algorithm(), key, nonce, sealed), StandardCharsets.UTF_8);
    }

    private static byte[] run(final boolean encrypt, final CryptoAlgorithm algorithm, final byte[] key, final byte[] nonce,
                              final byte[] input) {
        GCMModeCipher cipher = GCMBlockCipher.newInstance(algorithm.getEngine().get());
        cipher.init(encrypt, new AEADParameters(new KeyParameter(key), TAG_BITS, nonce));
        byte[] output = new byte[cipher.getOutputSize(input.length)];
        int length = cipher.processBytes(input, 0, input.length, output, 0);
        try {
            length += cipher.doFinal(output, length);
        } catch (InvalidCipherTextException e) {
            throw new IllegalStateException("암호문을 복호화할 수 없습니다. 키가 맞지 않거나 데이터가 손상되었습니다.", e);
        }
        return length == output.length ? output : Arrays.copyOf(output, length);
    }

    public record Header(CryptoAlgorithm algorithm, String keyId, String body) {
    }
}

package com.dgmoonlabs.cms.domain.common.social;

import com.dgmoonlabs.cms.domain.common.social.client.AppleSignIn;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class AppleSignInTest {
    @Test
    void signsTheClientSecretWithTheP8Key() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair keys = generator.generateKeyPair();
        // Pasted into a one-line field, so without line breaks.
        String p8 = "-----BEGIN PRIVATE KEY-----" + Base64.getEncoder().encodeToString(keys.getPrivate().getEncoded())
                + "-----END PRIVATE KEY-----";

        SignedJWT jwt = SignedJWT.parse(AppleSignIn.clientSecret("TEAM123456", "KEY1234567", "com.example.web", p8,
                Instant.parse("2026-11-04T00:00:00Z")));

        assertThat(jwt.verify(new ECDSAVerifier((ECPublicKey) keys.getPublic()))).isTrue();
        assertThat(jwt.getHeader().getKeyID()).isEqualTo("KEY1234567");
        assertThat(jwt.getJWTClaimsSet().getIssuer()).isEqualTo("TEAM123456");
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo("com.example.web");
        assertThat(jwt.getJWTClaimsSet().getAudience()).containsExactly("https://appleid.apple.com");
    }
}

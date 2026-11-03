package com.dgmoonlabs.cms.domain.common.social.client;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;

public final class AppleSignIn {
    public static final String ISSUER = "https://appleid.apple.com";
    public static final String JWK_SET_URI = "https://appleid.apple.com/auth/keys";

    private AppleSignIn() {
    }

    public static String clientSecret(final String teamId, final String keyId, final String clientId,
                                      final String privateKeyPem, final Instant now) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(teamId)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(300)))
                    .audience(ISSUER)
                    .subject(clientId)
                    .build();
            SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(keyId).build(), claims);
            jwt.sign(new ECDSASigner(parsePrivateKey(privateKeyPem)));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("애플 client secret 서명에 실패했습니다.", e);
        }
    }

    // 한 줄 입력란에 붙여 넣어 줄바꿈이 빠진 .p8 도 읽는다.
    static ECPrivateKey parsePrivateKey(final String pem) {
        try {
            String body = pem.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            return (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(body)));
        } catch (Exception e) {
            throw new IllegalArgumentException("애플 개인 키(.p8) 형식이 올바르지 않습니다.", e);
        }
    }

    // 검증기가 client ID·nonce 에 따라 달라서 호출마다 디코더를 새로 만든다.
    public static Jwt verify(final String jwkSetUri, final String idToken, final String clientId, final String nonce) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> audience = jwt -> jwt.getAudience() != null && jwt.getAudience().contains(clientId)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "audience mismatch", null));
        OAuth2TokenValidator<Jwt> nonceCheck = jwt -> nonce == null || Objects.equals(nonce, jwt.getClaimAsString("nonce"))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "nonce mismatch", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(ISSUER), audience, nonceCheck));
        return decoder.decode(idToken);
    }
}

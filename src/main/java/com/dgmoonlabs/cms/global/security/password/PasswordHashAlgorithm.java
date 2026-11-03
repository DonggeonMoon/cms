package com.dgmoonlabs.cms.global.security.password;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// id 는 해시 앞에 붙는 {접두어}로, PasswordEncoderFactories 와 같은 값이어야 한다.
@Getter
@RequiredArgsConstructor
public enum PasswordHashAlgorithm {
    BCRYPT("bcrypt", "bcrypt", "Spring Security 기본값. 널리 쓰이고 검증된 알고리즘입니다. 비밀번호는 앞 72바이트만 반영됩니다."),
    ARGON2("argon2@SpringSecurity_v5_8", "Argon2id", "메모리를 많이 쓰게 해 GPU 공격에 강합니다. OWASP 1순위 권장 알고리즘입니다."),
    SCRYPT("scrypt@SpringSecurity_v5_8", "scrypt", "메모리 사용량이 큰 알고리즘으로 대량 대입 공격에 강합니다."),
    PBKDF2("pbkdf2@SpringSecurity_v5_8", "PBKDF2-HMAC-SHA256", "FIPS-140 인증이 필요한 환경에서 쓰는 표준 알고리즘입니다.");

    private final String id;
    private final String label;
    private final String description;
}

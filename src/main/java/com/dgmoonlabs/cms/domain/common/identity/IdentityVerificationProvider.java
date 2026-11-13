package com.dgmoonlabs.cms.domain.common.identity;

import com.dgmoonlabs.cms.domain.common.identity.dto.IdentityVerificationDtos.VerificationResult;
import com.dgmoonlabs.cms.domain.common.identity.dto.IdentityVerificationDtos.VerificationSession;

/**
 * 본인인증(PASS, 아이핀, 통신사 인증 등) 연동 지점. 이 저장소는 특정 인증사를 직접 구현하지 않고,
 * 실제 연동 시 이 인터페이스의 구현체를 빈으로 등록하면 {@link IdentityVerificationConfig}의 기본
 * 구현({@link NoopIdentityVerificationProvider})을 대신 쓰게 된다.
 */
public interface IdentityVerificationProvider {
    /** 인증 화면(앱·웹)으로 이동시킬 때 필요한 정보를 돌려준다. */
    VerificationSession start(String returnUrl);

    /** 인증사 콜백에서 받은 토큰으로 인증 결과를 조회한다. */
    VerificationResult complete(String token);
}

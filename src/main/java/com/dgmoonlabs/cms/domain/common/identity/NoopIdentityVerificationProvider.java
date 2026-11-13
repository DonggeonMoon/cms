package com.dgmoonlabs.cms.domain.common.identity;

import com.dgmoonlabs.cms.domain.common.identity.dto.IdentityVerificationDtos.VerificationResult;
import com.dgmoonlabs.cms.domain.common.identity.dto.IdentityVerificationDtos.VerificationSession;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoopIdentityVerificationProvider implements IdentityVerificationProvider {
    @Override
    public VerificationSession start(final String returnUrl) {
        log.warn("[identity-verification] 연동된 본인인증 수단이 없어 요청을 처리할 수 없습니다.");
        throw new BusinessException(ErrorCode.IDENTITY_VERIFICATION_NOT_CONFIGURED);
    }

    @Override
    public VerificationResult complete(final String token) {
        log.warn("[identity-verification] 연동된 본인인증 수단이 없어 요청을 처리할 수 없습니다.");
        throw new BusinessException(ErrorCode.IDENTITY_VERIFICATION_NOT_CONFIGURED);
    }
}

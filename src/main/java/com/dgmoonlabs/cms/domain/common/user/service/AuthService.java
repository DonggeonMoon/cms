package com.dgmoonlabs.cms.domain.common.user.service;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.common.user.dto.LoginRequest;
import com.dgmoonlabs.cms.domain.common.user.entity.admin.entity.Admin;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.AdminRepository;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.LoginUser;
import com.dgmoonlabs.cms.global.security.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    @Transactional(noRollbackFor = BusinessException.class)
    public LoginUser authenticate(final LoginRequest request) {
        return request.userTypeOrDefault() == UserType.ADMIN
                ? authenticateAdmin(request)
                : authenticateMember(request);
    }

    private LoginUser authenticateMember(final LoginRequest request) {
        Member member = memberRepository.findByUsernameAndDeletedAtIsNull(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        if (member.isLocked(securityProperties.maxLoginTryCount())) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            member.recordLoginFailure();
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        member.recordLoginSuccess();
        return new LoginUser(member.getId(), member.getUsername(), member.getName(), UserType.MEMBER);
    }

    private LoginUser authenticateAdmin(final LoginRequest request) {
        Admin admin = adminRepository.findByUsernameAndDeletedAtIsNull(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        if (admin.isLocked(securityProperties.maxLoginTryCount())) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            admin.recordLoginFailure();
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        admin.recordLoginSuccess();
        return new LoginUser(admin.getId(), admin.getUsername(), admin.getName(), UserType.ADMIN);
    }
}

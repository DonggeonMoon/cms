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
        if (passwordEncoder.upgradeEncoding(member.getPassword())) {
            member.changePassword(passwordEncoder.encode(request.password()));
        }
        member.recordLoginSuccess();
        return new LoginUser(member.getId(), member.getUsername(), member.displayName(), UserType.MEMBER);
    }

    @Transactional
    public LoginUser authenticateSocialMember(final Long memberId) {
        Member member = memberRepository.findById(memberId).filter(found -> found.getDeletedAt() == null).orElse(null);
        if (member == null) {
            return null;
        }
        if (member.isLocked(securityProperties.maxLoginTryCount())) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        member.recordLoginSuccess();
        return new LoginUser(member.getId(), member.getUsername(), member.displayName(), UserType.MEMBER);
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
        if (passwordEncoder.upgradeEncoding(admin.getPassword())) {
            admin.changePassword(passwordEncoder.encode(request.password()));
        }
        admin.recordLoginSuccess();
        return new LoginUser(admin.getId(), admin.getUsername(), admin.getName(), UserType.ADMIN);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public void verifyAdminPassword(final Long adminId, final String password) {
        Admin admin = adminRepository.findById(adminId)
                .filter(found -> found.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        if (admin.isLocked(securityProperties.maxLoginTryCount())) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (password == null || !passwordEncoder.matches(password, admin.getPassword())) {
            admin.recordLoginFailure();
            throw new BusinessException(ErrorCode.REAUTHENTICATION_FAILED);
        }
        admin.recordLoginSuccess();
    }
}

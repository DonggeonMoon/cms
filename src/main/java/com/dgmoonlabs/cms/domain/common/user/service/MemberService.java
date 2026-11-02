package com.dgmoonlabs.cms.domain.common.user.service;

import com.dgmoonlabs.cms.domain.common.user.dto.*;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberAddress;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberAgreement;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberOrganization;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberAddressRepository;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberAgreementRepository;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberOrganizationRepository;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberAddressRepository memberAddressRepository;
    private final MemberAgreementRepository memberAgreementRepository;
    private final MemberOrganizationRepository memberOrganizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    @Transactional
    public Long join(final MemberJoinRequest request) {
        if (!request.isPrivacyConsentAgreed()) {
            throw new BusinessException(ErrorCode.PRIVACY_CONSENT_REQUIRED);
        }
        if (!request.password().equals(request.passwordCheck())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
        if (memberRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        Long memberId = memberRepository.save(request.toEntity(passwordEncoder.encode(request.password()))).getId();
        memberAddressRepository.save(request.toAddress(memberId));
        memberAgreementRepository.save(request.toAgreement(memberId));
        memberOrganizationRepository.save(request.toOrganization(memberId));
        return memberId;
    }

    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(final String username) {
        return !memberRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMember(final Long id) {
        Member member = findActiveMember(id);
        return MemberResponse.of(
                member,
                memberAddressRepository.findByMemberId(id).orElse(null),
                memberAgreementRepository.findByMemberId(id).orElse(null),
                memberOrganizationRepository.findByMemberId(id).orElse(null)
        );
    }

    @Transactional(readOnly = true)
    public Page<MemberSummaryResponse> searchMembers(final String keyword, final Pageable pageable) {
        int maxLoginTryCount = securityProperties.maxLoginTryCount();
        return memberRepository.search(keyword, pageable)
                .map(member -> MemberSummaryResponse.of(member, maxLoginTryCount));
    }

    @Transactional
    public void modify(final Long id, final MemberModifyRequest request) {
        findActiveMember(id).updateProfile(request);

        memberAddressRepository.findByMemberId(id)
                .orElseGet(() -> memberAddressRepository.save(MemberAddress.builder().memberId(id).build()))
                .update(request.postalCode(), request.address(), request.detailAddress());
        memberAgreementRepository.findByMemberId(id)
                .orElseGet(() -> memberAgreementRepository.save(MemberAgreement.builder().memberId(id).build()))
                .update(request.isMailReceptionAgreed(), request.isSmsReceptionAgreed(), request.isMarketingUsageAgreed());
        memberOrganizationRepository.findByMemberId(id)
                .orElseGet(() -> memberOrganizationRepository.save(MemberOrganization.builder().memberId(id).build()))
                .update(request.officeName(), request.officeDepartment(), request.officePosition(),
                        request.officeAddress(), request.officeDetailAddress());
    }

    @Transactional
    public void changePassword(final Long id, final PasswordChangeRequest request) {
        Member member = findActiveMember(id);
        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }
        if (!request.newPassword().equals(request.newPasswordCheck())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
        member.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void unlock(final Long id) {
        findActiveMember(id).unlock();
    }

    @Transactional
    public void quit(final Long id) {
        findActiveMember(id).delete();
    }

    private Member findActiveMember(final Long id) {
        return memberRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }
}

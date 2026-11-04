package com.dgmoonlabs.cms.domain.common.user.service;

import com.dgmoonlabs.cms.domain.common.user.dto.*;
import com.dgmoonlabs.cms.domain.common.policy.constant.PolicyType;
import com.dgmoonlabs.cms.domain.common.policy.service.PolicyService;
import com.dgmoonlabs.cms.domain.common.user.constant.PersonalInfoItem;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberJoinSetting;
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
import com.dgmoonlabs.cms.domain.common.grade.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberAddressRepository memberAddressRepository;
    private final MemberAgreementRepository memberAgreementRepository;
    private final MemberOrganizationRepository memberOrganizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;
    private final MemberJoinSettingService joinSettingService;
    private final PolicyService policyService;
    private final GradeService gradeService;

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

        MemberJoinSetting setting = joinSettingService.current();
        MemberName memberName = MemberName.resolve(setting, request.name(), request.lastName(), request.firstName());
        String nickname = resolveNickname(setting, request.nickname(), null);

        Long memberId = memberRepository.save(
                request.toEntity(passwordEncoder.encode(request.password()), memberName, nickname, setting)).getId();
        memberAddressRepository.save(request.toAddress(memberId, setting));
        memberAgreementRepository.save(request.toAgreement(memberId, policyService::isUsing));
        memberOrganizationRepository.save(request.toOrganization(memberId, setting));
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
        Function<Member, String> gradeName = gradeService.gradeNamer();
        return memberRepository.search(keyword, pageable)
                .map(member -> MemberSummaryResponse.of(member, maxLoginTryCount, gradeName.apply(member)));
    }

    @Transactional
    public void modify(final Long id, final MemberModifyRequest request) {
        MemberJoinSetting setting = joinSettingService.current();
        MemberName memberName = MemberName.resolve(setting, request.name(), request.lastName(), request.firstName());
        findActiveMember(id).updateProfile(request, memberName, resolveNickname(setting, request.nickname(), id), setting);

        memberAddressRepository.findByMemberId(id)
                .orElseGet(() -> memberAddressRepository.save(MemberAddress.builder().memberId(id).build()))
                .update(setting.keep(PersonalInfoItem.ADDRESS, request.postalCode()),
                        setting.keep(PersonalInfoItem.ADDRESS, request.address()),
                        setting.keep(PersonalInfoItem.ADDRESS, request.detailAddress()));
        memberAgreementRepository.findByMemberId(id)
                .orElseGet(() -> memberAgreementRepository.save(MemberAgreement.builder().memberId(id).build()))
                .update(request.isMailReceptionAgreed() && policyService.isUsing(PolicyType.MAIL_RECEPTION),
                        request.isSmsReceptionAgreed() && policyService.isUsing(PolicyType.SMS_RECEPTION),
                        request.isMarketingUsageAgreed() && policyService.isUsing(PolicyType.MARKETING_USAGE));
        memberOrganizationRepository.findByMemberId(id)
                .orElseGet(() -> memberOrganizationRepository.save(MemberOrganization.builder().memberId(id).build()))
                .update(setting.keep(PersonalInfoItem.OFFICE, request.officeName()),
                        setting.keep(PersonalInfoItem.OFFICE, request.officeDepartment()),
                        setting.keep(PersonalInfoItem.OFFICE, request.officePosition()),
                        setting.keep(PersonalInfoItem.OFFICE, request.officeAddress()),
                        setting.keep(PersonalInfoItem.OFFICE, request.officeDetailAddress()));
    }

    private String resolveNickname(final MemberJoinSetting setting, final String requested, final Long memberId) {
        if (!setting.isCollectNickname()) {
            return null;
        }
        if (requested == null || requested.isBlank()) {
            throw new BusinessException(ErrorCode.NICKNAME_REQUIRED);
        }
        String nickname = requested.trim();
        boolean taken = memberId == null
                ? memberRepository.existsByNicknameAndDeletedAtIsNull(nickname)
                : memberRepository.existsByNicknameAndIdNotAndDeletedAtIsNull(nickname, memberId);
        if (taken) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        return nickname;
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

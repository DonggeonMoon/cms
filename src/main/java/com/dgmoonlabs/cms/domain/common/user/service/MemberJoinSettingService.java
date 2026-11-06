package com.dgmoonlabs.cms.domain.common.user.service;

import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.service.RegexService;
import com.dgmoonlabs.cms.domain.common.user.dto.MemberJoinSettingDtos.AdminMemberJoinSettingResponse;
import com.dgmoonlabs.cms.domain.common.user.dto.MemberJoinSettingDtos.MemberJoinSettingRequest;
import com.dgmoonlabs.cms.domain.common.user.dto.MemberJoinSettingDtos.MemberJoinSettingResponse;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberJoinSetting;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberJoinSettingRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberJoinSettingService {
    private final MemberJoinSettingRepository repository;
    private final RegexService regexService;

    @Transactional(readOnly = true)
    public MemberJoinSetting current() {
        return repository.findFirstByOrderByIdAsc().orElseGet(MemberJoinSetting::defaults);
    }

    @Transactional(readOnly = true)
    public MemberJoinSettingResponse get() {
        return MemberJoinSettingResponse.from(current());
    }

    @Transactional(readOnly = true)
    public AdminMemberJoinSettingResponse getForAdmin() {
        return AdminMemberJoinSettingResponse.of(current(), regexService.selected(RegexTarget.Group.MEMBER));
    }

    @Transactional
    public AdminMemberJoinSettingResponse update(final MemberJoinSettingRequest request) {
        MemberJoinSetting setting = repository.findFirstByOrderByIdAsc()
                .orElseGet(() -> repository.save(MemberJoinSetting.defaults()));
        setting.update(request.collectNameOrDefault(), request.nameFormat(), request.collectNicknameOrDefault(),
                request.birthDateInputOrDefault(), request.collectedItems(), request.useMobilePhoneCountryCodeOrDefault());
        if (request.regexes() != null) {
            request.regexes().forEach((target, regexId) -> {
                if (target.getGroup() != RegexTarget.Group.MEMBER) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT);
                }
                regexService.select(target, regexId);
            });
        }
        return AdminMemberJoinSettingResponse.of(setting, regexService.selected(RegexTarget.Group.MEMBER));
    }
}

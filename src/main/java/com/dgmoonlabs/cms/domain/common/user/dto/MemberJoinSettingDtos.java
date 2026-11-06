package com.dgmoonlabs.cms.domain.common.user.dto;

import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.user.constant.BirthDateInput;
import com.dgmoonlabs.cms.domain.common.user.constant.NameFormat;
import com.dgmoonlabs.cms.domain.common.user.constant.PersonalInfoItem;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberJoinSetting;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class MemberJoinSettingDtos {
    private MemberJoinSettingDtos() {
    }

    // regexes: 값이 null 이면 검사 안 함, 빠진 키는 그대로 둔다.
    public record MemberJoinSettingRequest(Boolean collectName, @NotNull NameFormat nameFormat, Boolean collectNickname,
                                           BirthDateInput birthDateInput, @NotNull Set<PersonalInfoItem> collectedItems,
                                           Boolean useMobilePhoneCountryCode, Map<RegexTarget, Long> regexes) {
        public boolean collectNameOrDefault() {
            return collectName == null || collectName;
        }

        public boolean collectNicknameOrDefault() {
            return Boolean.TRUE.equals(collectNickname);
        }

        public BirthDateInput birthDateInputOrDefault() {
            return birthDateInput == null ? BirthDateInput.SELECT : birthDateInput;
        }

        public boolean useMobilePhoneCountryCodeOrDefault() {
            return Boolean.TRUE.equals(useMobilePhoneCountryCode);
        }
    }

    public record MemberJoinSettingResponse(boolean collectName, NameFormat nameFormat, boolean collectNickname,
                                            BirthDateInput birthDateInput, Set<PersonalInfoItem> collectedItems,
                                            boolean useMobilePhoneCountryCode) {
        public static MemberJoinSettingResponse from(final MemberJoinSetting setting) {
            return new MemberJoinSettingResponse(setting.isCollectName(), setting.getNameFormat(),
                    setting.isCollectNickname(), setting.getBirthDateInput(),
                    new TreeSet<>(setting.getCollectedItems()), setting.isUseMobilePhoneCountryCode());
        }
    }

    public record AdminMemberJoinSettingResponse(boolean collectName, NameFormat nameFormat, boolean collectNickname,
                                                 BirthDateInput birthDateInput, Set<PersonalInfoItem> collectedItems,
                                                 boolean useMobilePhoneCountryCode, Map<RegexTarget, Long> regexes) {
        public static AdminMemberJoinSettingResponse of(final MemberJoinSetting setting, final Map<RegexTarget, Long> regexes) {
            return new AdminMemberJoinSettingResponse(setting.isCollectName(), setting.getNameFormat(),
                    setting.isCollectNickname(), setting.getBirthDateInput(), new TreeSet<>(setting.getCollectedItems()),
                    setting.isUseMobilePhoneCountryCode(), regexes);
        }
    }
}

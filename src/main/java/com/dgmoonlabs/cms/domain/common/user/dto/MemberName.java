package com.dgmoonlabs.cms.domain.common.user.dto;

import com.dgmoonlabs.cms.domain.common.user.constant.NameFormat;
import com.dgmoonlabs.cms.domain.common.user.entity.member.MemberJoinSetting;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;

public record MemberName(String name, String lastName, String firstName) {
    public static MemberName resolve(final MemberJoinSetting setting, final String name, final String lastName,
                                     final String firstName) {
        if (!setting.isCollectName()) {
            return new MemberName(null, null, null);
        }
        if (setting.getNameFormat() == NameFormat.SPLIT) {
            if (isBlank(lastName) || isBlank(firstName)) {
                throw new BusinessException(ErrorCode.MEMBER_NAME_REQUIRED);
            }
            return new MemberName(lastName.trim() + firstName.trim(), lastName.trim(), firstName.trim());
        }
        if (isBlank(name)) {
            throw new BusinessException(ErrorCode.MEMBER_NAME_REQUIRED);
        }
        return new MemberName(name.trim(), null, null);
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}

package com.dgmoonlabs.cms.domain.common.guestposting;

import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;

public record GuestPostingPolicy(boolean allowed, boolean collectPhone, boolean collectEmail) {
    @JsonIgnore
    @AssertTrue(message = "연락처와 이메일 중 적어도 하나는 받아야 합니다.")
    public boolean isContactChosen() {
        return collectPhone || collectEmail;
    }

    public void requireContacts(final String phoneNumber, final String email) {
        if (collectPhone && isBlank(phoneNumber) || collectEmail && isBlank(email)) {
            throw new BusinessException(ErrorCode.GUEST_CONTACT_REQUIRED);
        }
    }

    public String keepPhone(final String phoneNumber) {
        return collectPhone && !isBlank(phoneNumber) ? phoneNumber.trim() : null;
    }

    public String keepEmail(final String email) {
        return collectEmail && !isBlank(email) ? email.trim() : null;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}

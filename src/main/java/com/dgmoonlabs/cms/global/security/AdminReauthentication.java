package com.dgmoonlabs.cms.global.security;

import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

public final class AdminReauthentication {
    private static final String SESSION_ATTRIBUTE = "CMS_ADMIN_REAUTHENTICATED";
    private static final Duration VALIDITY = Duration.ofMinutes(5);

    private AdminReauthentication() {
    }

    public static void grant(final HttpServletRequest request, final Long adminId) {
        request.getSession(true).setAttribute(SESSION_ATTRIBUTE, new Grant(adminId, Instant.now()));
    }

    public static void require(final HttpServletRequest request, final Long adminId) {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute(SESSION_ATTRIBUTE);
        if (!(value instanceof Grant grant) || !grant.adminId().equals(adminId)
                || grant.verifiedAt().plus(VALIDITY).isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.REAUTHENTICATION_REQUIRED);
        }
        session.setAttribute(SESSION_ATTRIBUTE, new Grant(adminId, Instant.now()));
    }

    public static void revoke(final HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_ATTRIBUTE);
        }
    }

    private record Grant(Long adminId, Instant verifiedAt) implements Serializable {
    }
}

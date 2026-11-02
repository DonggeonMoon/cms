package com.dgmoonlabs.cms.global.security;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class CurrentUser {
    private CurrentUser() {
    }

    public static Optional<LoginUser> get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return Optional.of(loginUser);
        }
        return Optional.empty();
    }

    public static boolean isAdmin() {
        return get().map(user -> user.userType() == UserType.ADMIN).orElse(false);
    }

    public static boolean isMember() {
        return get().map(user -> user.userType() == UserType.MEMBER).orElse(false);
    }

    public static boolean is(final UserType userType, final Long userId) {
        return get().map(user -> user.userType() == userType && user.id().equals(userId)).orElse(false);
    }
}

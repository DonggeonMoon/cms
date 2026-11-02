package com.dgmoonlabs.cms.global.security;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;

import java.io.Serializable;

public record LoginUser(Long id, String username, String name, UserType userType) implements Serializable {
    public String role() {
        return "ROLE_" + userType.name();
    }
}

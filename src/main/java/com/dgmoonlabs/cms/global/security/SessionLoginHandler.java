package com.dgmoonlabs.cms.global.security;

import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.security.ZonedSecurityContextRepository.Zone;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SessionLoginHandler {
    private final ZonedSecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy holderStrategy = SecurityContextHolder.getContextHolderStrategy();

    public void login(final LoginUser loginUser, final HttpServletRequest request, final HttpServletResponse response) {
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                loginUser, null, List.of(new SimpleGrantedAuthority(loginUser.role())));

        SecurityContext context = holderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        holderStrategy.setContext(context);

        request.getSession(true);
        request.changeSessionId();
        Zone zone = loginUser.userType() == UserType.ADMIN ? Zone.ADMIN : Zone.SITE;
        if (zone == Zone.ADMIN) {
            AdminReauthentication.revoke(request);
        }
        securityContextRepository.saveContext(zone, context, request, response);
    }

    public void logout(final HttpServletRequest request, final HttpServletResponse response) {
        Zone zone = ZonedSecurityContextRepository.zoneOf(request);
        if (zone == Zone.ADMIN) {
            AdminReauthentication.revoke(request);
        }
        securityContextRepository.clear(zone, request);
        holderStrategy.clearContext();
    }
}

package com.dgmoonlabs.cms.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.DeferredSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class ZonedSecurityContextRepository implements SecurityContextRepository {
    public static final String ZONE_HEADER = "X-CMS-Zone";
    static final String SITE_KEY = "CMS_SECURITY_CONTEXT_SITE";
    static final String ADMIN_KEY = "CMS_SECURITY_CONTEXT_ADMIN";

    public enum Zone {
        SITE, ADMIN
    }

    private final HttpSessionSecurityContextRepository site = repository(SITE_KEY);
    private final HttpSessionSecurityContextRepository admin = repository(ADMIN_KEY);

    public static Zone zoneOf(final HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean adminZone = path.startsWith("/api/admin/") || "admin".equalsIgnoreCase(request.getHeader(ZONE_HEADER));
        return adminZone ? Zone.ADMIN : Zone.SITE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public SecurityContext loadContext(final HttpRequestResponseHolder holder) {
        return repositoryFor(zoneOf(holder.getRequest())).loadContext(holder);
    }

    @Override
    public DeferredSecurityContext loadDeferredContext(final HttpServletRequest request) {
        return repositoryFor(zoneOf(request)).loadDeferredContext(request);
    }

    @Override
    public void saveContext(final SecurityContext context, final HttpServletRequest request, final HttpServletResponse response) {
        repositoryFor(zoneOf(request)).saveContext(context, request, response);
    }

    public void saveContext(final Zone zone, final SecurityContext context, final HttpServletRequest request,
                            final HttpServletResponse response) {
        repositoryFor(zone).saveContext(context, request, response);
    }

    @Override
    public boolean containsContext(final HttpServletRequest request) {
        return repositoryFor(zoneOf(request)).containsContext(request);
    }

    public void clear(final Zone zone, final HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(zone == Zone.ADMIN ? ADMIN_KEY : SITE_KEY);
        }
    }

    private HttpSessionSecurityContextRepository repositoryFor(final Zone zone) {
        return zone == Zone.ADMIN ? admin : site;
    }

    private static HttpSessionSecurityContextRepository repository(final String key) {
        HttpSessionSecurityContextRepository repository = new HttpSessionSecurityContextRepository();
        repository.setSpringSecurityContextKey(key);
        return repository;
    }
}

package com.dgmoonlabs.cms.domain.admin.logging.aop;

import com.dgmoonlabs.cms.domain.admin.logging.entity.WatchdogLog;
import com.dgmoonlabs.cms.domain.admin.logging.service.WatchdogLogService;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

//@Aspect
//@Component
@RequiredArgsConstructor
public class WatchdogAspect {
    private final WatchdogLogService watchdogLogService;

    @Pointcut("execution(public * *.*AdminController.*(..))")
    public void admin() {
    }

    @After("admin()")
    public void afterAdminMethods(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        if (!HttpMethod.POST.matches(request.getMethod())) {
            return;
        }
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        String[] parameterNames = methodSignature.getParameterNames();
        Object[] parameterValues = joinPoint.getArgs();


        Member currentMember = (Member) request.getSession().getAttribute("currentMember");

        watchdogLogService.save(WatchdogLog.builder()
                .memberUsername("")
                .uri(request.getRequestURI())
                .methodName(methodSignature.getName())
                .content("")
                .ipAddress(request.getRemoteAddr())
                .build()
        );
    }
}

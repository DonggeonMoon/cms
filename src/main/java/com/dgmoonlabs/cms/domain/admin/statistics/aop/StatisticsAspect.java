package com.dgmoonlabs.cms.domain.admin.statistics.aop;

import com.dgmoonlabs.cms.domain.admin.statistics.service.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

//@Aspect
//@Component
@RequiredArgsConstructor
public class StatisticsAspect {
    private final StatisticsService statisticsService;

    @Pointcut("execution(public * *.*UserController.*(..))")
    public void user() {
    }

    @After("user()")
    public Object updateStatistics(MethodInvocation invocation) throws Throwable {
        HttpServletRequest request;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            request = attributes.getRequest();

            statisticsService.updateStatistics(request);
        }

        return invocation.proceed();
    }
}
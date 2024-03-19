package com.dgmoonlabs.cms.domain.admin.statistics.aop;

import com.dgmoonlabs.cms.domain.admin.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Pointcut;

//@Aspect
//@Component
@RequiredArgsConstructor
public class StatisticsAspect {
    private final StatisticsService statisticsService;

    @Pointcut("execution(public * *.*UserController.*(..))")
    public void user() {
    }

    @After("admin()")
    public void afterUserMethods(JoinPoint joinPoint) {
    }
}
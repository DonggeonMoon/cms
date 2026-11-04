package com.dgmoonlabs.cms.domain.admin.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 개인정보를 내보내는 조회처럼, GET 이라도 관리자 감사 로그에 남겨야 하는 요청. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditedRead {
}

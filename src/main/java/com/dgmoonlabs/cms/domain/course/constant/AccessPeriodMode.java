package com.dgmoonlabs.cms.domain.course.constant;

/** 교육과정으로 사용하는 시리즈에만 적용되는 수강 가능 기간 방식. 일반 시리즈는 항상 UNLIMITED처럼 동작한다. */
public enum AccessPeriodMode {
    UNLIMITED, FIXED, DAYS_FROM_ENROLLMENT
}

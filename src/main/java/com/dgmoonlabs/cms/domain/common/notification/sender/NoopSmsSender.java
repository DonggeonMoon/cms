package com.dgmoonlabs.cms.domain.common.notification.sender;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoopSmsSender implements SmsSender {
    @Override
    public void send(final String to, final String title, final String content) {
        log.warn("[sms] 발송기가 설정되지 않아 발송을 건너뜁니다: to={}, title={}", to, title);
    }
}

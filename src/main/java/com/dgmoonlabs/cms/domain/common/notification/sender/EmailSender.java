package com.dgmoonlabs.cms.domain.common.notification.sender;

/** 실제 발송기 연동 시 구현체를 빈으로 등록한다. */
public interface EmailSender {
    void send(String to, String title, String content);
}

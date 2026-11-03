package com.dgmoonlabs.cms.domain.common.regex.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegexTarget {
    USERNAME("아이디", Group.MEMBER, "^[a-z0-9_]{4,20}$",
            "아이디는 영문 소문자, 숫자, _ 로 4~20자여야 합니다.", "영문 소문자, 숫자, _ 4~20자", null),
    PASSWORD("비밀번호", Group.MEMBER, "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
            "비밀번호는 영문과 숫자를 포함해 8~64자로 입력해 주세요.", "영문·숫자 포함 8~64자", null),
    NICKNAME("닉네임", Group.MEMBER, "^[가-힣A-Za-z0-9_]{2,20}$",
            "닉네임은 한글, 영문, 숫자, _ 로 2~20자여야 합니다.", "한글·영문·숫자·_ 2~20자", null),
    EMAIL("이메일", Group.MEMBER, "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            "이메일 형식이 올바르지 않습니다.", "name@example.com", null),
    MOBILE_PHONE("휴대폰 번호", Group.MEMBER, "^01[016789]-?\\d{3,4}-?\\d{4}$",
            "휴대폰 번호 형식이 올바르지 않습니다. (예: 010-1234-5678)", "010-1234-5678", null),
    PHONE_NUMBER("전화번호", Group.MEMBER, "^0\\d{1,2}-?\\d{3,4}-?\\d{4}$",
            "전화번호 형식이 올바르지 않습니다. (예: 02-123-4567, 010-1234-5678)", "02-123-4567, 010-1234-5678", null),
    POSTAL_CODE("우편번호", Group.MEMBER, "^\\d{5}$",
            "우편번호는 숫자 5자리여야 합니다.", "숫자 5자리", null),
    ORDER_NUMBER("주문번호", Group.NUMBER, "^O\\d{14}-\\d{4}$",
            "주문번호 형식이 올바르지 않습니다.", "O20261103153000-1234", "O"),
    RESERVATION_NUMBER("예약번호", Group.NUMBER, "^R\\d{14}-\\d{4}$",
            "예약번호 형식이 올바르지 않습니다.", "R20261103153000-1234", "R"),
    PAYMENT_NUMBER("결제번호", Group.NUMBER, "^P\\d{14}-\\d{4}$",
            "결제번호 형식이 올바르지 않습니다.", "P20261103153000-1234", "P");

    public enum Group {
        MEMBER,
        NUMBER
    }

    private final String label;
    private final Group group;
    private final String defaultPattern;
    private final String defaultMessage;
    private final String defaultHint;
    private final String numberPrefix;
}

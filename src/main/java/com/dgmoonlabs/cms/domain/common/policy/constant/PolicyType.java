package com.dgmoonlabs.cms.domain.common.policy.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PolicyType {
    PRIVACY_CONSENT("개인정보 수집·이용 동의", """
            <p><strong>[작성 필요] 관리자 &gt; 공통 &gt; 개인정보 수집·이용 동의에서 실제 내용으로 수정해 주세요.</strong></p>
            <h3>1. 수집하는 개인정보 항목</h3>
            <p>필수: 아이디, 비밀번호, 이름 / 선택: 이메일, 연락처, 주소, 직장 정보</p>
            <h3>2. 수집·이용 목적</h3>
            <p>회원 식별 및 가입 의사 확인, 서비스 제공, 문의 응대</p>
            <h3>3. 보유 및 이용 기간</h3>
            <p>회원 탈퇴 시까지. 다만 관계 법령에 따라 보존이 필요한 경우 해당 기간 동안 보관합니다.</p>
            <h3>4. 동의를 거부할 권리</h3>
            <p>동의를 거부할 수 있으며, 거부 시 회원가입이 제한됩니다.</p>
            """),
    PRIVACY_POLICY("개인정보처리방침", """
            <p><strong>[작성 필요] 관리자 &gt; 공통 &gt; 개인정보처리방침에서 실제 내용으로 수정해 주세요.</strong></p>
            <h3>제1조 (개인정보의 처리 목적)</h3>
            <p>회사는 회원 관리, 서비스 제공, 민원 처리를 위해 개인정보를 처리합니다.</p>
            <h3>제2조 (처리하는 개인정보 항목 및 보유 기간)</h3>
            <p>회원가입 시 수집한 항목을 회원 탈퇴 시까지 보유합니다.</p>
            <h3>제3조 (정보주체의 권리)</h3>
            <p>정보주체는 언제든지 개인정보 열람·정정·삭제·처리정지를 요구할 수 있습니다.</p>
            <h3>제4조 (개인정보 보호책임자)</h3>
            <p>성명: / 연락처:</p>
            """);

    private final String label;
    private final String defaultContentHtml;
}

package egovframework.com.cms.member.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TermsAgreement {
    private boolean useTermsAgreement;//약관동의
    private boolean privacyTermsAgreement;//개인정보 수집 및 이용동의
}

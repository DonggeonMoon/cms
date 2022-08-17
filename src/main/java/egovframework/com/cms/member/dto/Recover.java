package egovframework.com.cms.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Recover {
    private String recoverMode;
    private String memberId;
    private String memberName;
    private String memberMobile1;
    private String memberMobile2;
    private String memberMobile3;
    private String memberEmail;
    private String memberTelMobile;

    @Override
    public String toString() {
        return "Recover\n[recoverMode=" + recoverMode + "\n, memberId=" + memberId + "\n, memberName=" + memberName + "\n, memberMobile1=" + memberMobile1 + "\n, memberMobile2=" + memberMobile2
                + "\n, memberMobile3=" + memberMobile3 + "\n, memberEmail=" + memberEmail + "\n, memberTelMobile=" + memberTelMobile + "]\n";
    }
}
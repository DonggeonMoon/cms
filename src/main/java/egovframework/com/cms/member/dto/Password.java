package egovframework.com.cms.member.dto;

import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Password {
    @WatchDog
    private String memberId;
    private String oldPassword;
    private String newPassword;
    private String newPasswordCheck;
    private Date memberPWLastUpdated;

    @Override
    public String toString() {
        return String.format("Password [\nmemberId=%s\n, oldPassword=%s\n, newPassword=%s\n, newPasswordCheck=%s\n]", memberId, oldPassword, newPassword, newPasswordCheck);
    }

}

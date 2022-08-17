package egovframework.com.cms.login.dto;

import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Login {
    @WatchDog
    private String loginId;
    private String loginPassword;
    private boolean rememberMe;
    private String returnUrl;
    private boolean signUpKeyCheck;
}

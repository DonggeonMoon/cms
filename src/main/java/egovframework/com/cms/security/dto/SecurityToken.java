package egovframework.com.cms.security.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SecurityToken {
    private String token;
    private String timeMillis;
}

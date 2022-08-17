package egovframework.com.cms.remotepublish.dto;

import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemotePublishFacebookConfig {
    @WatchDog
    private String facebookUse;
    private String facebookAppId;
    private String facebookAppSecret;
    private String facebookPageId;
    private String facebookPageAccessToken;//expired never
}

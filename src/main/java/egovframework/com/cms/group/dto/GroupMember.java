package egovframework.com.cms.group.dto;

import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupMember {
    @WatchDog
    private String groupId;
    @WatchDog
    private String memberId;
}

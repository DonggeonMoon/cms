package egovframework.com.cms.member.dto;

import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MemberDelete {
    private Integer memberDeleteId;
    @WatchDog
    private String memberId;
    private String memberDeletedBy;
    private Date memberRegDate;
    private Date memberDeleteDate;
    private Member currentUser;
    private boolean readMemberDeleteInfo;
}

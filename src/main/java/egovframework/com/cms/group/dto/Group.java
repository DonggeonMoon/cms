package egovframework.com.cms.group.dto;

import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Group {
    @WatchDog
    private String groupId;
    private String groupName;
    private Group groupParent;
    private Date groupRegDate;
    private int groupOrder;
    private int groupDepth;

    private List<Group> groupChildren;

    private int groupMemberTotal;

    //Set 에서 중복제거하기 위해서 오버라이딩함(이클립스로 코드 생성)
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        return result;
    }

    // Set 에서 중복제거하기 위해서 오버라이딩함(이클립스로 코드 생성)
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Group other = (Group) obj;
        if (groupId == null) {
            if (other.groupId != null)
                return false;
        } else if (!groupId.equals(other.groupId))
            return false;
        return true;
    }
}

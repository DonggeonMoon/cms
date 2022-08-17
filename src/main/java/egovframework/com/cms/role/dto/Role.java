package egovframework.com.cms.role.dto;

import egovframework.com.cms.capability.dto.Capability;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Role implements Serializable {
    @WatchDog
    private String roleCode;
    private String roleName;
    private String roleDescription = "";//필수필드 아니라서 초기값 넣어줌(""으로 안해주면 오라클에서 에러)
    private Date roleRegDate;
    private boolean roleDefault;
    private List<Capability> capabilityList;
    private boolean roleAdmin;//관리자 권한인지 -> 코드는 바뀔수도 있으니까 bool값으로 하나 둔다.
    private boolean roleJoin = false;

    @Override
    public String toString() {
        return this.getRoleCode();
    }
}

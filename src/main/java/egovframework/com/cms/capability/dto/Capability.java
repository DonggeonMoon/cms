package egovframework.com.cms.capability.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class Capability extends MultiSiteVO implements Serializable {
    @WatchDog
    private String capId;
    private String capName;
    private String capDescription;
    private String capValue;
    private String capParam1Key;
    private String capParam1Value;
    private String capParam2Key;
    private String capParam2Value;
    private String capHttpMethod;//ALL or GET or POST
    private Date capRegDate;
    private boolean capDefault;
    private int capPriority = 100;//우선순위-값이 큰 권한이 먼저 체크됨 기본값 100, 범위가 큰 권한일수록 값이 작음 ex) /admin/.* => 1 VS /admin/aaa/bbb.do => 200
    private boolean capHidden = false;//숨김여부 - 보여줄필요 없는 권한의 경우 디비에서 컬럼값 수정하는 방식으로 처리

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((capId == null) ? 0 : capId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Capability other = (Capability) obj;
        if (capId == null) {
            if (other.capId != null)
                return false;
        } else if (!capId.equals(other.capId))
            return false;
        return true;
    }
}

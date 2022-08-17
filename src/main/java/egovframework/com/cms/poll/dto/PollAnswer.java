package egovframework.com.cms.poll.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PollAnswer extends MultiSiteVO {
    private Integer paId;
    private Integer poId; //투표
    private String paText; //보기내용
    private Integer paOrder;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((paId == null) ? 0 : paId.hashCode());
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
        PollAnswer other = (PollAnswer) obj;
        if (paId == null) {
            if (other.paId != null)
                return false;
        } else if (!paId.equals(other.paId))
            return false;
        return true;
    }
}

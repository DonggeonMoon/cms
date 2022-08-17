package egovframework.com.cms.bulkmail.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BulkMailTo {
    private Integer bmtId;
    private BulkMail bulkMail;
    private String bmtAddress;
    private String bmtService;
    private String bmtName;//없을수도 있음
    private boolean bmtSended = false;
    private String bmtSendDate;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bmtAddress == null) ? 0 : bmtAddress.hashCode());
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
        BulkMailTo other = (BulkMailTo) obj;
        if (bmtAddress == null) {
            if (other.bmtAddress != null)
                return false;
        } else if (!bmtAddress.equals(other.bmtAddress))
            return false;
        return true;
    }
}

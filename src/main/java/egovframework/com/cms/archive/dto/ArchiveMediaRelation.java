package egovframework.com.cms.archive.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArchiveMediaRelation extends MultiSiteVO {
    private Integer arcId;
    private Integer fileId;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((arcId == null) ? 0 : arcId.hashCode());
        result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
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
        ArchiveMediaRelation other = (ArchiveMediaRelation) obj;
        if (arcId == null) {
            if (other.arcId != null)
                return false;
        } else if (arcId.intValue() != other.arcId.intValue())
            return false;
        if (fileId == null) {
            if (other.fileId != null)
                return false;
        } else if (fileId.intValue() != other.fileId.intValue())
            return false;
        return true;
    }
}

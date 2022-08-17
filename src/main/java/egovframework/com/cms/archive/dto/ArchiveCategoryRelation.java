package egovframework.com.cms.archive.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArchiveCategoryRelation extends MultiSiteVO {
    private Integer arcId;
    private Integer catId;
}

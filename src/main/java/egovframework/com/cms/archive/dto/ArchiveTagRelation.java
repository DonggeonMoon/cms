package egovframework.com.cms.archive.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArchiveTagRelation extends MultiSiteVO {
    private Integer arcId;
    private Integer tagId;
}

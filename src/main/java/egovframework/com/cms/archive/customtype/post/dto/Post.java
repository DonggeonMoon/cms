package egovframework.com.cms.archive.customtype.post.dto;

import egovframework.com.cms.archive.dto.Archive;
import egovframework.com.cms.support.annotation.ArchiveItem;
import lombok.Getter;
import lombok.Setter;

@ArchiveItem(customType = "post", label = "포스트")
@Getter
@Setter
public class Post extends Archive {
    private boolean poSticky = false;
    private boolean poSelected1 = false;
    private boolean poSelected2 = false;
    private String poStickySDate;
    private String poStickyEDate;
    private Integer poStickyOrder;
}

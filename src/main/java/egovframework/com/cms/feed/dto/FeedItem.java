package egovframework.com.cms.feed.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class FeedItem extends MultiSiteVO {
    private String feedId;
    private String feedTitle;
    private String feedLink;
    private String feedDescription;
    private Date feedPubDate;
    private String feedAuthor;
    private String feedGuid;
}

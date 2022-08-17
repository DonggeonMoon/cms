package egovframework.com.cms.feed.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class FeedConfig extends MultiSiteVO {
    @WatchDog
    private Integer feedConfigId;//generated id
    @WatchDog
    private String feedUriName;

    private String feedType = "rss";//rss, atom - 일단 rss만 구현해 놓음
    private String feedVersion = "2.0";//
    private String feedChannelTitle;
    private String feedChannelLink;
    private String feedChannelDescription;
    private String feedChannelCopyright;

    private String feedTable;
    private String feedPKColumn;
    private String feedModuleIdColumn;
    private String feedModuleSubIdColumn;
    private String feedTitleColumn;
    private String feedLinkColumn;
    private String feedDescriptionColumn;
    private String feedPubDateColumn;
    private String feedAuthorColumn;
    private String feedGuidColumn;
    private Integer feedPageSize = 20;//기본 출력개수
    private Integer feedDescriptionLength = 200;//본문요약길이
    private Date feedConfigRegDate;

    private String feedModule;
    private String feedModuleId;
    private String feedModuleSub;
    private String feedModuleSubId;

    private String feedExtraCondition;
}

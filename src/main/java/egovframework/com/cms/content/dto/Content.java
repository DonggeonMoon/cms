package egovframework.com.cms.content.dto;

import egovframework.com.cms.member.dto.Member;
import egovframework.com.cms.menu.dto.Menu;
import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class Content extends MultiSiteVO implements Serializable {
    @WatchDog
    private Menu menu;
    private String content = "";
    private String contentPlain = "";
    private String contentStatus = "draft";
    @WatchDog
    private Integer contentVer = 0;
    private Date contentRegDate;
    private Date contentLastModified;
    private Member contentLastWorker;
    private String contentMemo = "";
}

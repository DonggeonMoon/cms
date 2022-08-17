package egovframework.com.cms.schedule.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleEvent extends MultiSiteVO {
    private Integer seId;

    private String seStartDate;//2014-06-01
    private Integer seStartHour = 9;
    private Integer seStartMin = 0;
    private String seEndDate;
    private Integer seEndHour = 18;
    private Integer seEndMin = 0;
    private boolean seWholeDay = false;//하루종일
    @WatchDog
    private String seTitleKr;
    private String seTitleEn;
    private String seMemo;

    private String seLinkUrl;
    private String seLinkTarget = "_self";

    private String seColor = "transparent";
}

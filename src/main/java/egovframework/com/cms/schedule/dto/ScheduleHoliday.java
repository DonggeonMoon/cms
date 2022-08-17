package egovframework.com.cms.schedule.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

@Getter
@Setter
public class ScheduleHoliday extends MultiSiteVO {
    private Integer shId;
    private Integer shYear = Calendar.getInstance().get(Calendar.YEAR);
    private Integer shMonth;
    private Integer shDay;
    @WatchDog
    private String shTitleKr;
    private String shTitleEn;
    private boolean shRecur = true;
    private boolean shLunar = false;//음력이냐
    private String shMemo;
}

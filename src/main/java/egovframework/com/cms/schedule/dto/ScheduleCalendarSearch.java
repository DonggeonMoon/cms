package egovframework.com.cms.schedule.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.support.exception.EcmsException;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

@Getter
@Setter
public class ScheduleCalendarSearch extends MultiSiteVO {

    private Integer calYear;
    private Integer calMonth;//+1된 월
    private String calFormat = "html_default";// json 등등 일단 html만 구현

    private Integer prevYear;
    private Integer prevMonth;
    private Integer nextYear;
    private Integer nextMonth;

    public Integer getCalYear() {
        if (this.calYear == null || this.calYear == 0) {
            return Calendar.getInstance().get(Calendar.YEAR);
        }
        return calYear;
    }

    public void setCalYear(Integer calYear) {
        this.calYear = calYear;
    }

    public Integer getCalMonth() {
        if (this.calYear == null || this.calYear == 0) {
            return Calendar.getInstance().get(Calendar.MONTH) + 1;
        }
        return calMonth;
    }

    public void buildCalPrevNextParameters() {
        if (this.getCalYear() == null || this.getCalMonth() == null) {
            throw new EcmsException("calYear or calMonth is missing.");
        }
        if (this.getCalMonth() == 1) {
            this.setPrevYear(this.getCalYear() - 1);
            this.setPrevMonth(12);
            this.setNextYear(this.getCalYear());
            this.setNextMonth(2);
        } else if (this.getCalMonth() == 12) {
            this.setPrevYear(this.getCalYear());
            this.setPrevMonth(11);
            this.setNextYear(this.getCalYear() + 1);
            this.setNextMonth(1);
        } else {
            this.setPrevYear(this.getCalYear());
            this.setPrevMonth(this.getCalMonth() - 1);
            this.setNextYear(this.getCalYear());
            this.setNextMonth(this.getCalMonth() + 1);
        }
    }
}

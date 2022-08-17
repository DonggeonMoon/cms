package egovframework.com.cms.schedule.dto;

import egovframework.com.cms.support.exception.EcmsException;
import egovframework.com.cms.support.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Getter
@Setter
public class ScheduleDay {
    private Integer year;
    private Integer month;
    private Integer day;
    private int dayOfWeek;//요일
    private boolean holiday = false;//휴일여부
    private List<ScheduleHoliday> scheduleHolidayList;//이 날에 해당하는 휴일만 들어 있음
    private List<ScheduleEvent> scheduleEventList;

    public void populateHoliday(int calYear, List<ScheduleHoliday> scheduleHolidayList) {
        if (scheduleHolidayList == null || scheduleHolidayList.isEmpty()) {
            this.holiday = false;
        } else {
            if (this.getScheduleHolidayList() == null) {
                this.scheduleHolidayList = new ArrayList<ScheduleHoliday>();
            }
            for (ScheduleHoliday sh : scheduleHolidayList) {
                //음력휴일이면
                if (sh.isShLunar()) {
                    //반복되는 휴일이면
                    if (sh.isShRecur()) {
                        Calendar solarCal = Utils.fromLunarCalToSolarCal(calYear, sh.getShMonth(), sh.getShDay());
                        if (this.getYear().intValue() == solarCal.get(Calendar.YEAR)
                                && this.getMonth().intValue() == solarCal.get(Calendar.MONTH) + 1
                                && this.getDay().intValue() == solarCal.get(Calendar.DAY_OF_MONTH)) {
                            //음력날짜 표시
                            if (sh.getShDay().intValue() == Utils.LUNAR_LAST_DAY_OF_MONTH) {
                                sh.setShTitleKr(sh.getShTitleKr() + "(" + sh.getShMonth() + "/末日)");
                            } else {
                                sh.setShTitleKr(sh.getShTitleKr() + "(" + sh.getShMonth() + "/" + sh.getShDay() + ")");
                            }
                            this.setHoliday(true);
                            this.scheduleHolidayList.add(sh);

                        }
                    }
                    //하루짜리면
                    else {
                        //음력휴일이면서 반복 아닌거는 못만들게 해야 한다 - 밸리데이터에서 거르고 있으므로 이 예외가 발생할 일은 없다...고 보는게.
                        throw new EcmsException("no way");
                    }
                }
                //양력휴일이면
                else {
                    //반복되는 휴일이면 월까지만 체크
                    if (sh.isShRecur()) {
                        if (this.getMonth().intValue() == sh.getShMonth().intValue()
                                && this.getDay().intValue() == sh.getShDay().intValue()) {
                            this.setHoliday(true);
                            this.scheduleHolidayList.add(sh);
                        }
                    }
                    //하루짜리 휴일(선거일 같은거)이면 년도까지 체크
                    else {
                        if (this.getYear().intValue() == sh.getShYear().intValue()
                                && this.getMonth().intValue() == sh.getShMonth().intValue()
                                && this.getDay().intValue() == sh.getShDay().intValue()) {
                            this.setHoliday(true);
                            this.scheduleHolidayList.add(sh);
                        }
                    }
                }
            }
        }
    }

    private Date getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        Date date = null;
        try {
            date = sdf.parse(this.getYear() + "-" + String.format("%02d", this.getMonth()) + "-" + String.format("%02d", this.getDay()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        date = Utils.makeNoon(date);
        return date;
    }

    public void populateEvent(List<ScheduleEvent> scheduleEventList) {
        if (this.scheduleEventList == null) {
            this.scheduleEventList = new ArrayList<ScheduleEvent>();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        Date date = this.getDate();
        try {
            for (ScheduleEvent se : scheduleEventList) {
                if (date.getTime() <= Utils.makeNoon(sdf.parse(se.getSeEndDate())).getTime() && date.getTime() >= Utils.makeNoon(sdf.parse(se.getSeStartDate())).getTime()) {
                    this.scheduleEventList.add(se);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
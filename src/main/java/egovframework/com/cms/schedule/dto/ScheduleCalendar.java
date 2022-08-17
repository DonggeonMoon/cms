package egovframework.com.cms.schedule.dto;

import egovframework.com.cms.support.exception.EcmsException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
public class ScheduleCalendar {
    private Integer calYear;
    private Integer calMonth;

    private List<ScheduleEvent> scheduleEventList;
    private List<ScheduleHoliday> scheduleHolidayList;

    private List<ScheduleDay> days;

    public ScheduleCalendar() {
        throw new EcmsException("Use ScheduleCalendar(ScheduleCalendarSearch scheduleCalendarSearch, List<ScheduleHoliday> scheduleHolidayList, List<ScheduleEvent> scheduleEventList) instead...");
    }

    public ScheduleCalendar(ScheduleCalendarSearch scheduleCalendarSearch, List<ScheduleHoliday> scheduleHolidayList, List<ScheduleEvent> scheduleEventList) {

        this.scheduleHolidayList = scheduleHolidayList;
        this.scheduleEventList = scheduleEventList;

        int year = scheduleCalendarSearch.getCalYear();
        int month = scheduleCalendarSearch.getCalMonth() - 1;

        this.setCalYear(scheduleCalendarSearch.getCalYear());
        this.setCalMonth(scheduleCalendarSearch.getCalMonth());

        //지금 보여줄 달
        Calendar currentMonthCal = Calendar.getInstance(Locale.KOREA);
        currentMonthCal.set(Calendar.YEAR, year);
        currentMonthCal.set(Calendar.MONTH, month);
        currentMonthCal.set(Calendar.DAY_OF_MONTH, 1);

        //이전달
        Calendar lastMonthCal = Calendar.getInstance(Locale.KOREA);
        lastMonthCal.set(Calendar.YEAR, year);
        lastMonthCal.set(Calendar.MONTH, month);
        lastMonthCal.add(Calendar.MONTH, -1);

        //다음달
        Calendar nextMonthCal = Calendar.getInstance(Locale.KOREA);
        nextMonthCal.set(Calendar.YEAR, year);
        nextMonthCal.set(Calendar.MONTH, month);
        nextMonthCal.add(Calendar.MONTH, 1);

        //이번달 마지막 날
        int lastDayOfCurrentMonth = currentMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        //1:일요일 ~ 2:화요일...
        //1일의 요일
        currentMonthCal.set(Calendar.DAY_OF_MONTH, 1);

        int dayOfWeekStartDay = currentMonthCal.get(Calendar.DAY_OF_WEEK);

        //마지막날의 요일
        currentMonthCal.set(Calendar.DAY_OF_MONTH, currentMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        int dayOfWeekLastDay = currentMonthCal.get(Calendar.DAY_OF_WEEK);
        int lastDayOfLastMonth = lastMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        //달력에 들어갈 날들을 채우자
        days = new ArrayList<ScheduleDay>();
        ScheduleDay scheduleDay = null;

        //달력에 표시될 지난달 끝부분
        int dayOfWeek = 1;
        int startDayOfLastMonth = lastDayOfLastMonth;
        if (dayOfWeekStartDay == 1) {
            startDayOfLastMonth = startDayOfLastMonth - 6;
        } else {
            startDayOfLastMonth = startDayOfLastMonth - dayOfWeekStartDay + 2;
        }
        for (int i = startDayOfLastMonth; i <= lastDayOfLastMonth; i++) {
            scheduleDay = new ScheduleDay();
            scheduleDay.setYear(lastMonthCal.get(Calendar.YEAR));
            scheduleDay.setMonth(lastMonthCal.get(Calendar.MONTH) + 1);
            scheduleDay.setDay(i);
            scheduleDay.setDayOfWeek(dayOfWeek++);
            days.add(scheduleDay);
        }

        //이번달부분
        for (int i = 1; i <= lastDayOfCurrentMonth; i++) {
            scheduleDay = new ScheduleDay();
            scheduleDay.setYear(currentMonthCal.get(Calendar.YEAR));
            scheduleDay.setMonth(currentMonthCal.get(Calendar.MONTH) + 1);
            scheduleDay.setDay(i);
            dayOfWeek = dayOfWeek % 7 == 0 ? 7 : dayOfWeek % 7;
            scheduleDay.setDayOfWeek(dayOfWeek++);
            days.add(scheduleDay);
        }

        //달력에 표시될 다음달 시작부분
        int lastDayOfNextMonth = 1;
        if (dayOfWeekLastDay == 7) {
            lastDayOfNextMonth = 7;
        } else {
            lastDayOfNextMonth = 7 - dayOfWeekLastDay;
        }
        for (int i = 1; i <= lastDayOfNextMonth; i++) {
            scheduleDay = new ScheduleDay();
            scheduleDay.setYear(nextMonthCal.get(Calendar.YEAR));
            scheduleDay.setMonth(nextMonthCal.get(Calendar.MONTH) + 1);
            scheduleDay.setDay(i);
            dayOfWeek = dayOfWeek % 7 == 0 ? 7 : dayOfWeek % 7;
            scheduleDay.setDayOfWeek(dayOfWeek++);
            days.add(scheduleDay);
        }

        //휴일 공휴일 설정
        for (ScheduleDay sd : days) {
            sd.populateHoliday(scheduleCalendarSearch.getCalYear(), scheduleHolidayList);
        }
        //이벤트 할당
        for (ScheduleDay sd : days) {
            sd.populateEvent(scheduleEventList);
        }
    }
}

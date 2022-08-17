package egovframework.com.cms.schedule.dto;

import egovframework.com.cms.support.exception.EcmsException;
import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

@Getter
@Setter
public class ScheduleEventSearch extends PagingSearch {
    private String fromDate;
    private String toDate;

    private Integer calYear;
    private Integer calMonth;

    public void buildSearchDate(Integer calYear, Integer calMonth) {
        this.calYear = calYear;
        this.calMonth = calMonth;
        this.buildSearchDate();
    }

    public void buildSearchDate() {
        if (this.calYear == null || this.calYear == 0 || this.calMonth == null || this.calMonth == 0) {
            throw new EcmsException("event search year, month missing.");
        }
        Calendar fromCal = Calendar.getInstance();
        fromCal.set(Calendar.YEAR, this.calYear);
        fromCal.set(Calendar.MONTH, this.calMonth - 1);
        fromCal.add(Calendar.MONTH, -1);
        this.fromDate = fromCal.get(Calendar.YEAR) + "-" + String.format("%02d", fromCal.get(Calendar.MONTH) + 1) + "-20";

        Calendar toCal = Calendar.getInstance();
        toCal.set(Calendar.YEAR, this.calYear);
        toCal.set(Calendar.MONTH, this.calMonth - 1);
        toCal.add(Calendar.MONTH, 1);
        this.toDate = toCal.get(Calendar.YEAR) + "-" + String.format("%02d", toCal.get(Calendar.MONTH) + 1) + "-10";
    }
}

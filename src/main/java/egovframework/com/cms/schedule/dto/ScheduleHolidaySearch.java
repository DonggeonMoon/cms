package egovframework.com.cms.schedule.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleHolidaySearch extends PagingSearch {
    private Integer shYear = 0;
}

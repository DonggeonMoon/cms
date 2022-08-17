package egovframework.com.cms.statistics.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

@Getter
@Setter
public class StatDay extends MultiSiteVO {
    private Integer statId;
    private String statItemType;
    private String statItemId;
    private Integer statYear;
    private Integer statMonth;
    private String statDay;

    public StatDay() {
        Calendar cal = Calendar.getInstance();
        this.statYear = cal.get(Calendar.YEAR);
        this.statMonth = cal.get(Calendar.MONTH) + 1;
        this.statDay = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
    }
}

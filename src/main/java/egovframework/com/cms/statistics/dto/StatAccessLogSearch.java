package egovframework.com.cms.statistics.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class StatAccessLogSearch extends PagingSearch {
    private String logYear;
    private String logMonth;
    private String logTableName;

    public String getLogTableName() {
        if (StringUtils.isBlank(this.logTableName)) {
            return "ECMS_STAT_LOG_" + this.logYear + this.logMonth;
        }
        return logTableName;
    }

    public void setLogTableName(String logTableName) {
        this.logTableName = logTableName;
    }

    @Override
    public String getQueryString() {

        StringBuilder sb = new StringBuilder(100);
        sb.append(super.getQueryString());

        if (StringUtils.isNotBlank(this.getLogYear())) {
            sb.append("&amp;logYear=");
            sb.append(this.getLogYear());
        }

        if (StringUtils.isNotBlank(this.getLogMonth())) {
            sb.append("&amp;logMonth=");
            sb.append(this.getLogMonth());
        }

        return sb.toString();
    }
}

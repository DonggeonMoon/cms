package egovframework.com.cms.statistics.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatSearch extends PagingSearch {
    private String statItemType;
    private String statItemId;
    private Integer statYear;
    private Integer statMonth;
    private boolean dashboard;

    @Override
    public String toString() {
        return "StatSearch [statItemType=" + statItemType + ", statItemId=" + statItemId + ", statYear=" + statYear + ", statMonth=" + statMonth + ", dashboard=" + dashboard + "]";
    }
}

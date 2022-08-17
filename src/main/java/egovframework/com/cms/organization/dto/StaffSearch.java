package egovframework.com.cms.organization.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffSearch extends PagingSearch {
    private Integer stDepIdx;

    @Override
    public String getQueryString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(super.getQueryString());

        if (this.getStDepIdx() != null && this.getStDepIdx() > 0) {
            sb.append("&amp;stDepIdx=");
            sb.append(this.getStDepIdx().intValue());
        }
        return sb.toString();
    }
}

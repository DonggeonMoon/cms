package egovframework.com.cms.board.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class BoardConfigSearch extends PagingSearch {
    private String bcStatus;

    @Override
    public String getQueryString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(super.getQueryString());

        if (StringUtils.isNotBlank(this.getBcStatus())) {
            sb.append("&amp;bcStatus=");
            sb.append(this.getBcStatus());
        }

        return sb.toString();
    }
}

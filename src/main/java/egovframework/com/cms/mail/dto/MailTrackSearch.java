package egovframework.com.cms.mail.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class MailTrackSearch extends PagingSearch {
    private String mtOpened;

    @Override
    public String getQueryString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(super.getQueryString());

        if (StringUtils.isNotBlank(this.getMtOpened())) {
            sb.append("&amp;mtOpened=");
            sb.append(this.getMtOpened());
        }
        return sb.toString();
    }
}

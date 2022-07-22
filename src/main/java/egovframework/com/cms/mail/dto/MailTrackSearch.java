package egovframework.com.cms.mail.dto;

import egovframework.com.cms.support.Pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class MailTrackSearch extends PagingSearch {
    private String mailTrackOpened;

    @Override
    public String getQueryString() {
        StringBuilder stringBuilder = new StringBuilder(100);
        stringBuilder.append(super.getQueryString());

        if (StringUtils.isNotBlank(getMailTrackOpened())) {
            stringBuilder.append("&amp;mailTrackedOpened");
            stringBuilder.append(getMailTrackOpened());
        }

        return stringBuilder.toString();
    }
}

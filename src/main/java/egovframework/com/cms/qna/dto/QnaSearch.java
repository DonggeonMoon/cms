package egovframework.com.cms.qna.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class QnaSearch extends PagingSearch {
    private String qnaOpen; //글숨김 여부

    @Override
    public String getQueryString() {
        StringBuilder sb = new StringBuilder(500);
        sb.append(super.getQueryString());

        if (StringUtils.isNotBlank(this.getQnaOpen())) {
            sb.append("&amp;qnaOpen=");
            sb.append(this.getQnaOpen());
        }
        return sb.toString();
    }
}

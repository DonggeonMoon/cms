package egovframework.com.cms.content.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class ContentSearch extends PagingSearch {
    private String menuId;
    private String contentStatus;
    private Integer contentVer = 0;

    //삭제할때 임시로 사용
    private String vers;

    @Override
    public String getQueryString() {

        StringBuilder sb = new StringBuilder(100);
        sb.append(super.getQueryString());

        if (StringUtils.isNotBlank(this.getMenuId())) {
            sb.append("&amp;menu.menuId=");
            sb.append(this.getMenuId());
        }

        if (this.getContentVer() > 0) {
            sb.append("&amp;contentVer=");
            sb.append(this.getContentVer());
        }

        return sb.toString();
    }
}

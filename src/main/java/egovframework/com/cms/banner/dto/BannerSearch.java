package egovframework.com.cms.banner.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class BannerSearch extends PagingSearch {
    private String bnType = "mainVisual";
    private String bnUse; //true|false
    private String bnToday;

    @Override
    public String getQueryString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(super.getQueryString());

        if (StringUtils.isNotBlank(this.getBnType())) {
            sb.append("&amp;bnType=");
            sb.append(this.getBnType());
        }

        return sb.toString();
    }
}

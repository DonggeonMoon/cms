package egovframework.com.cms.config.dto;

import egovframework.com.cms.support.Pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigSearch extends PagingSearch {
    private String confId;
    private String sitePrefix;
}

package egovframework.com.cms.advancedsearch.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdvancedSearch extends PagingSearch {
    private String searchTarget;
    private String menuType;
}

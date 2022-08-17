package egovframework.com.cms.code.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeSearch extends PagingSearch {
    private String codeUse;
    private String catId;
}

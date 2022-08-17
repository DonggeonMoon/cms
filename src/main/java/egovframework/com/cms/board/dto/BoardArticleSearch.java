package egovframework.com.cms.board.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardArticleSearch extends PagingSearch {
    private String bcId;
    private String baCategory1;
    private String baCategory2;
    private String baCategory3;
    private boolean baNotice;
}

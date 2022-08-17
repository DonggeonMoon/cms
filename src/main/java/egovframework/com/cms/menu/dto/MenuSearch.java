package egovframework.com.cms.menu.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuSearch extends PagingSearch {
    private Integer menuDepth;
    private String menuStatus;

    //uri로 메뉴 찾을때 사용
    private String menuLink;
    private String menuLinkSearchCondition;
    private String menuType;

    public MenuSearch() {
        super();
        this.setPaging(false);
    }
}

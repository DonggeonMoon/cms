package egovframework.com.cms.group.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupSearch extends PagingSearch {
    private String parentGroupId;
    private int groupDepth;

    public GroupSearch() {
        super();
        this.setPaging(false);
    }
}

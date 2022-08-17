package egovframework.com.cms.bulkmail.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BulkMailToSearch extends PagingSearch {
    private Integer bmId;
}

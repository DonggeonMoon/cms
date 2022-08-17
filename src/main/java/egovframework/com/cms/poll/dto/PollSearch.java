package egovframework.com.cms.poll.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PollSearch extends PagingSearch {
    private Integer poId;
}

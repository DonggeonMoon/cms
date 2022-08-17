package egovframework.com.cms.archive.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArchiveTagSearch extends PagingSearch {
    private String term;
    private Integer arcId;
}

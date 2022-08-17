package egovframework.com.cms.capability.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CapabilitySearch extends PagingSearch {
    private String roleCode;
}

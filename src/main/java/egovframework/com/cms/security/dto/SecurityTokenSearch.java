package egovframework.com.cms.security.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class SecurityTokenSearch extends PagingSearch {
    private String timeMillis;
    private String token;
}

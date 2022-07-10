package egovframework.com.cms.faq.dto;

import egovframework.com.cms.support.Pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FaqSearch extends PagingSearch {
    private String question;
    private Boolean publish;
}

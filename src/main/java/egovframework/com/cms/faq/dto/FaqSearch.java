package egovframework.com.cms.faq.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FaqSearch extends PagingSearch {
    private String question;
    private Boolean publish;
}

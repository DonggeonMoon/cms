package egovframework.com.cms.advancedsearch.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdvancedSearchResult {
    private Integer total;
    private List<AdvancedSearchItem> list;
}
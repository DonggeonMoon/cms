package egovframework.com.cms.code.dto;

import egovframework.com.cms.code.model.Code;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CodeCategory {
    @WatchDog
    private String catId;
    private String catName;
    private String catNameEn;
    private String catDescription;
    private Date catRegDate;
    private List<Code> codeList;
}

package egovframework.com.cms.code.model;

import egovframework.com.cms.code.dto.CodeCategory;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Code {
    @WatchDog
    private String codeId;
    @WatchDog
    private CodeCategory codeCategory;
    private String codeName;
    private String codeNameEn;
    private String codeDescription;
    private Date codeRegDate;
    private boolean codeUse = true;//기본값 사용
    private Integer codeOrder;
}

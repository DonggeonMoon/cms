package egovframework.com.cms.organization.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Department {
    private Integer depIdx;
    private Integer depOrder;
    private String depName;//부서명
    private String depTel;//textarea
    private String depDescription;//부서소개
    private String depRemark;//비고 기타
}

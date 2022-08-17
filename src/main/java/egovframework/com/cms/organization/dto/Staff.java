package egovframework.com.cms.organization.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Staff {
    private Integer stIdx;
    private Integer stDepIdx;//부서아이디
    private Integer stOrder;
    private String stRankCode;//직급 코드
    private String stName;
    private String stWork;
    private String stTel;
    private String stRemark;//비고
    private boolean stActive = true;//재직중
}

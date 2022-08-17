package egovframework.com.cms.poll.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PollVote extends MultiSiteVO {
    private Integer pvId;
    private Integer poId;
    private Integer paId;
    private String paEtc; //기타 답변
    private String pvUnique;
}

package egovframework.com.cms.qna.dto;

import egovframework.com.cms.member.dto.Member;
import egovframework.com.cms.site.dto.MultiSiteVO;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Getter
@Setter
public class Qna extends MultiSiteVO {
    private Integer qnaIdx;
    private Integer qnaFamily;//본글일 경우 자기 idx, 답글일 경우 부모글 idx 들어감
    private String qnaTitle;
    private String qnaContent;
    private Member qnaMember;
    private boolean qnaSecret;
    private String qnaEmail;//회원작성시 회원이메일 받아와서 저장하고 이후로는 걍 별도로 감
    private String qnaGuestName;
    private String qnaGuestPassword;
    private String qnaIp;
    private Date qnaCreated;
    private boolean qnaOpen;//숨기기
    private Integer qnaHit;
    private boolean qnaDeleted;//삭제여부

    public boolean isGuestArticle() {
        return this.getQnaMember() == null || StringUtils.isBlank(this.getQnaMember().getMemberId()) || StringUtils.isNotBlank(this.getQnaGuestPassword());
    }

    public boolean isQuestion() {

        //최초 작성할때는 idx 가 null 이다
        if (this.getQnaIdx() == null) {
            return true;
        }

        return this.getQnaIdx().intValue() == this.getQnaFamily().intValue();
    }
}

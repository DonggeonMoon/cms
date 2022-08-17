package egovframework.com.cms.comment.dto;

import egovframework.com.cms.member.model.Member;
import egovframework.com.cms.menu.dto.Menu;
import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Comment extends MultiSiteVO {
    @WatchDog
    private Integer cmId;
    private Menu menu;
    private String cmBy = "";//MEMBER, SNS_FACEBOOK, SNS_TWITTER, GUEST
    private String cmModule = "";
    private String cmModuleId = "";
    private String cmModuleSub = "";
    private String cmModuleSubId = "";
    private String cmMemId;
    private Member member;//사이트계정으로 댓글 작성시 사용자 정보
    private Comment cmParent;//부모댓글
    private List<Comment> cmChildren;
    private Integer cmOrder = 0;
    private String cmTitle = "";//댓글이 작성되는 콘텐츠의 타이틀
    private String cmContent = "";
    private Integer cmRating;//점수, 별점
    private String cmPageUrl = "";
    private Boolean cmSnsSend = Boolean.FALSE;
    private String cmSnsUserId = "";
    private String cmSnsUserName = "";
    private String cmSnsUserHome = "";
    private String cmSnsItemId = "";
    private String cmProfileImage = "";
    private Integer cmAgree = 0;//현재는 사용안함
    private Integer cmDisagree = 0;//현재는 사용안함
    private Integer cmRecommend = 0;
    private Integer cmReport = 0;
    private String cmStatus;//WAITING, APPROVED, MODERATED, DELETED
    private String cmModerateCode = "";//코드테이블사용
    private String cmModerateEtc = "";
    private String cmRegIp = "";
    private Date cmRegDate;

    private String cmGuestName; // 디비컬럼은 없음 SNS_USER_NAME으로 들어감
    private String cmGuestEmail; // 디비컬럼은 없음 SNS_USER_ID 로 들어감
    private String cmGuestPassword; //비회원 비밀번호 - 컬럼 있음

    public boolean isPureSnsComment() {
        if (this.getCmMemId().matches("temp_\\d{15}") || this.getCmMemId().matches("guest_\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}")) {
            return true;
        }
        return false;
    }
}

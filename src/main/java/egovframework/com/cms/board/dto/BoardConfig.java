package egovframework.com.cms.board.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class BoardConfig extends MultiSiteVO {
    @WatchDog
    private String bcId;
    private String bcStatus = "active";
    private String bcName;
    private String bcType = "default";//뷰파일폴더이름 = 게시판 유형 - default, gallery, audio, music
    private String bcCategory1;//code 테이블에서 가져옴
    private String bcCategory1Name = "카테고리1";
    private String bcCategory2;//code 테이블에서 가져옴
    private String bcCategory2Name = "카테고리2";
    private String bcCategory3;//code 테이블에서 가져옴
    private String bcCategory3Name = "카테고리3";
    private String bcStatusCatId;//code 테이블에서 가져옴
    private String bcListFile;
    private String bcViewFile;
    private String bcFormFile;

    private String bcModeratorCap;
    //사용자 그룹 지정 - 이 값이 있을 경우 일반회원 권한보다 우선한다.
    private String bcGroup;
    //권한별처리나 그룹별처리는 뷰파일에서 필요한 경우에만 커스터마이징 하는게 차라리 편하다(소스도 지저분해지지 않고...)
    private boolean bcAllowMemberList = true;
    private boolean bcAllowMemberView = true;
    private boolean bcAllowMemberForm = true;
    private boolean bcAllowMemberComment = true;//사용안함
    private boolean bcAllowMemberDownload = true;

    private boolean bcAllowGuestList = true;
    private boolean bcAllowGuestView = true;
    private boolean bcAllowGuestForm = false;
    private boolean bcAllowGuestComment = false;//사용안함
    private boolean bcAllowGuestDownload = false;

    private boolean bcSupportNotice = false;
    private boolean bcNoticeEveryPage = true;//first, every
    private boolean bcSupportSecret = false;
    private boolean bcSupportComment = true;
    private boolean bcSupportRecommend = true;

    private boolean bcSupportThumbnail = true;
    private boolean bcThumbnailCrop = true;
    private Integer bcThumbnailWidth = 200;
    private Integer bcThumbnailHeight = 150;

    private Integer bcUploadFileNum = 5;
    private Integer bcUploadSizeMax = 10;//MB단위, 서버설정을 오버할 수 없음

    private Date bcRegDate;
    private Integer bcPageSize = 10;

    private Integer bcArticleTotal;
}

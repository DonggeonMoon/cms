package egovframework.com.cms.board.dto;

import egovframework.com.cms.fileinfo.dto.FileInfo;
import egovframework.com.cms.member.dto.Member;
import egovframework.com.cms.site.dto.MultiSiteVO;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class BoardArticle extends MultiSiteVO {
    private Integer baId;
    private BoardConfig boardConfig;
    private String baCategory1 = "";
    private String baCategory2 = "";
    private String baCategory3 = "";
    private String baTitle;
    private String baContentHtml = "";//본문 html - CLOB
    private String baContentPlain = "";//본문 plain text - 1500자 제한
    private String baAnswer = "";//Qna 게시판일경우 답변 - 1500자 제한
    private boolean baNotice = false;
    private String baNoticeStartDate = "";
    private String baNoticeEndDate = "";
    private Member member;
    private String baGuestName = "";//비회원일 경우
    private String baEmail = "";
    private String baGuestPassword = "";
    private boolean baSecret = false;
    private String baSecretPassword = "";
    private Date baRegDate;
    private String baStatus = "";
    private String baUpdaterId = "";
    private Date baLastModified;
    private Integer baHit = 0;
    private String baIp = "";
    private Integer baCommentTotal = 0;
    private Integer baRecommend = 0;

    private List<FileInfo> baFileInfos;
    private List<String> baAltTexts;
    //컬럼 없음 - 파일첨부필드
    private List<MultipartFile> baMultipartFiles;
    //컬럼 없음 - 파일 삭제필드
    private List<Integer> baFileInfoDeleteIds;

    public boolean isGuestArticle() {
        if (this.getMember() != null) {
            if (this.getMember().getMemberId().matches("temp_\\d{15}") || this.getMember().getMemberId().matches("guest_\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}")) {
                return true;
            }
        }
        return StringUtils.isNotBlank(this.getBaGuestPassword());
    }

    public void setBaContentPlainFromBaContentHtml() {
        if (StringUtils.isNotBlank(this.getBaContentHtml())) {
            String content = Jsoup.clean(this.getBaContentHtml(), Whitelist.none());
            if (content.length() > 1500) {
                content = content.substring(0, 1500);
            }
            //검색용으로 본문의 텍스트만 따로 처리
            this.setBaContentPlain(content);
        }
    }
}

package egovframework.com.cms.clean.dto;

import egovframework.com.cms.support.util.Utils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.safety.Whitelist;

import java.util.Date;

@Getter
@Setter
public class Clean {
    private Integer clIdx;
    private String clName;
    private String clMobile1;
    private String clMobile2;
    private String clMobile3;
    private String clEmail;
    private String clTitle;
    private String clContent;
    private Date clCreated;
    private String clDupInfo;//아이핀, 휴대폰 인증

    private String clAnswer;//폼에서 답변받는 필드 - db컬럼은 없음
    //답변 - 5개까지만 일단 받게 - 굳이 테이블 새로 만들필요 있나 싶어서 걍 합침
    private String clAnswer1;
    private String clAnswer1Date;
    private String clAnswer2;
    private String clAnswer2Date;
    private String clAnswer3;
    private String clAnswer3Date;
    private String clAnswer4;
    private String clAnswer4Date;
    private String clAnswer5;
    private String clAnswer5Date;

    private Integer clAnswerNo;//답변 번호

    //답변개수
    public Integer getClAnswerCount() {
        int count = 0;
        if (StringUtils.isNotBlank(this.getClAnswer1())) {
            count++;
        }
        if (StringUtils.isNotBlank(this.getClAnswer2())) {
            count++;
        }
        if (StringUtils.isNotBlank(this.getClAnswer3())) {
            count++;
        }
        if (StringUtils.isNotBlank(this.getClAnswer4())) {
            count++;
        }
        if (StringUtils.isNotBlank(this.getClAnswer5())) {
            count++;
        }
        return count;
    }

    public void sanitize() {
        if (StringUtils.isNotBlank(this.getClAnswer())) {
            this.setClAnswer(Utils.getJsoupFilteredText(this.getClAnswer(), Whitelist.none(), true, false));
        }
        if (StringUtils.isNotBlank(this.getClName())) {
            this.setClName(Utils.getJsoupFilteredText(this.getClName(), Whitelist.none(), true, false));
        }
        if (StringUtils.isNotBlank(this.getClTitle())) {
            this.setClTitle(Utils.getJsoupFilteredText(this.getClTitle(), Whitelist.none(), true, false));
        }
        if (StringUtils.isNotBlank(this.getClContent())) {
            this.setClContent(Utils.getJsoupFilteredText(this.getClContent(), Whitelist.none(), true, false));
        }
        if (StringUtils.isNotBlank(this.getClMobile1())) {
            this.setClMobile1(Utils.getJsoupFilteredText(this.getClMobile1(), Whitelist.none(), true, false));
        }
        if (StringUtils.isNotBlank(this.getClMobile2())) {
            this.setClMobile2(Utils.getJsoupFilteredText(this.getClMobile2(), Whitelist.none(), true, false));
        }
        if (StringUtils.isNotBlank(this.getClMobile3())) {
            this.setClMobile3(Utils.getJsoupFilteredText(this.getClMobile3(), Whitelist.none(), true, false));
        }
    }
}
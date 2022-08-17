package egovframework.com.cms.banner.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Getter
@Setter
public class Banner extends MultiSiteVO {
    @WatchDog
    private Integer bnId;
    private String bnType = "banner"; //banner, popupzone, static, mainVisual, layer
    private Integer bnOrder;
    @WatchDog
    private String bnName;
    private String bnDescription;
    private String bnLink;
    private boolean bnNewWin = true;
    private String bnStartDate;//팝업존 시작일
    private String bnEndDate;//팝업존 종료일
    private Date bnRegDate;
    private boolean bnUse = true;
    private String bnExt;//파일 확장자

    private Integer bnWidth;//ex) 400
    private Integer bnHeight;//ex) 300
    private String bnPosition;//ex) left: 0; right: 0 -> css position 그대로 넣어주면됨

    private MultipartFile bnImage;

    @Override
    public String toString() {
        return String.format(
                "Banner [\nbnId=%s\n, bnType=%s\n, bnOrder=%s\n, bnName=%s\n, bnDescription=%s\n, bnLink=%s\n, bnNewWin=%s\n, bnStartDate=%s\n, bnEndDate=%s\n, bnRegDate=%s\n, bnUse=%s\n]", bnId,
                bnType, bnOrder, bnName, bnDescription, bnLink, bnNewWin, bnStartDate, bnEndDate, bnRegDate, bnUse);
    }
}

package egovframework.com.cms.satisfaction.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
public class Satisfaction extends MultiSiteVO {
    @WatchDog
    private String menuId;
    private Integer satScore;//컬럼 없음
    private Integer satScore50 = 0;
    private Integer satScore40 = 0;
    private Integer satScore30 = 0;
    private Integer satScore20 = 0;
    private Integer satScore10 = 0;
    private Date satLastRateDate;
    @WatchDog
    private Date satResetDate;

    public Satisfaction(String menuId, Date satLastRateDate, Date satResetDate) {
        this.menuId = menuId;
        this.satLastRateDate = satLastRateDate;
        this.satResetDate = satResetDate;
    }
}

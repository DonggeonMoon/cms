package egovframework.com.cms.statistics.dto;

import egovframework.com.cms.site.dto.MultiSiteVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Stat extends MultiSiteVO {
    private Integer statId;
    private String statItemType;
    private String statItemId;
    private Integer statYear;
    private Integer statMonth;
    private Integer statDay01 = 0;
    private Integer statDay02 = 0;
    private Integer statDay03 = 0;
    private Integer statDay04 = 0;
    private Integer statDay05 = 0;
    private Integer statDay06 = 0;
    private Integer statDay07 = 0;
    private Integer statDay08 = 0;
    private Integer statDay09 = 0;
    private Integer statDay10 = 0;
    private Integer statDay11 = 0;
    private Integer statDay12 = 0;
    private Integer statDay13 = 0;
    private Integer statDay14 = 0;
    private Integer statDay15 = 0;
    private Integer statDay16 = 0;
    private Integer statDay17 = 0;
    private Integer statDay18 = 0;
    private Integer statDay19 = 0;
    private Integer statDay20 = 0;
    private Integer statDay21 = 0;
    private Integer statDay22 = 0;
    private Integer statDay23 = 0;
    private Integer statDay24 = 0;
    private Integer statDay25 = 0;
    private Integer statDay26 = 0;
    private Integer statDay27 = 0;
    private Integer statDay28 = 0;
    private Integer statDay29 = 0;
    private Integer statDay30 = 0;
    private Integer statDay31 = 0;

    public Integer getDaysTotal() {
        return this.getStatDay01().intValue()
                + this.getStatDay02().intValue()
                + this.getStatDay03().intValue()
                + this.getStatDay04().intValue()
                + this.getStatDay05().intValue()
                + this.getStatDay06().intValue()
                + this.getStatDay07().intValue()
                + this.getStatDay08().intValue()
                + this.getStatDay09().intValue()
                + this.getStatDay10().intValue()
                + this.getStatDay11().intValue()
                + this.getStatDay12().intValue()
                + this.getStatDay13().intValue()
                + this.getStatDay14().intValue()
                + this.getStatDay15().intValue()
                + this.getStatDay16().intValue()
                + this.getStatDay17().intValue()
                + this.getStatDay18().intValue()
                + this.getStatDay19().intValue()
                + this.getStatDay20().intValue()
                + this.getStatDay21().intValue()
                + this.getStatDay22().intValue()
                + this.getStatDay23().intValue()
                + this.getStatDay24().intValue()
                + this.getStatDay25().intValue()
                + this.getStatDay26().intValue()
                + this.getStatDay27().intValue()
                + this.getStatDay28().intValue()
                + this.getStatDay29().intValue()
                + this.getStatDay30().intValue()
                + this.getStatDay31().intValue();
    }
}

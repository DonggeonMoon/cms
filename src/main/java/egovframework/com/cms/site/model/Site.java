package egovframework.com.cms.site.model;

import egovframework.com.cms.support.Constant;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ECMS_SITE")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Site {
    private static final String DOMAIN_GLUE = ",";

    @Id
    @WatchDog
    private String siteId;
    private String siteDomain;
    private String siteName;
    private String siteDescription;
    private String siteTheme;
    private boolean siteMain;
    private String siteLocale;

    private String siteType = "domain"; // 서브사이트 구분 유형: domain/dir

    public String getSiteLocale() {
        if (StringUtils.isBlank(this.siteLocale)) {
            return "ko_KR";
        }
        return siteLocale;
    }

    public String getSitePrefix() {
        if (Constant.MAIN_SITE_DISPLAY_ID.equals(this.getSiteId())) {
            return Constant.MAIN_SITE_PREFIX;
        }
        return this.getSiteId().toUpperCase();
    }

    public List<String> getSeperatedSiteDomains() {
        List<String> list = new ArrayList<String>();
        String domains = StringUtils.deleteWhitespace(this.getSiteDomain());
        if (StringUtils.isNotBlank(domains)) {
            if (!domains.contains(DOMAIN_GLUE)) {
                list.add(domains);
            } else {
                String[] temp = domains.split(DOMAIN_GLUE);
                for (String item : temp) {
                    list.add(item);
                }
            }
        }
        return list;
    }
}

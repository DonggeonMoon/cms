package egovframework.com.cms.support.pagination;

import egovframework.com.cms.config.dto.Config;
import egovframework.com.cms.config.service.ConfigOptionService;
import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.support.ApplicationContextProvider;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@Getter
@Setter
public class PagingSearch extends MultiSiteVO {
    protected String sc;
    protected String sv;

    protected String pageParamName = "cp";//페이지 파라메터
    protected Integer cp;//현재페이지
    protected Integer pageSize = 10;// 한페이지에 몇개
    protected int offset;// for MYSQL
    protected int startRow; // for ORACLE
    protected int endRow; // for ORACLE
    protected boolean paging = true;//페이징 적용 여부 기본값 : true

    protected String sortOrder;
    protected String sortDirection;
    protected String sortOverride;//프로그램단에서 강제로 정렬 덮을때, 파라메터로는 노출되지 않음

    private String openerCallback;//팝업 콜백함수 이름 처리용
    private String extra1;//임시 파라메터 다용도로 사용가능
    private String extra2;

    private String listType = "list";

    public void setSitePrefix(HttpServletRequest request) {
        Site site = (Site) request.getAttribute("currentSite");
        if (site != null) {
            this.sitePrefix = site.getSitePrefix();
        }
    }

    public String getQueryString() {
        StringBuilder sb = new StringBuilder(100);

        if (StringUtils.isNotBlank(this.getSc())) {
            sb.append("&amp;sc=");
            sb.append(this.getSc());
        }

        if (StringUtils.isNotBlank(this.getSv())) {
            sb.append("&amp;sv=");
            sb.append(this.getSv());
        }

        if (this.getPageSize() != 10 && this.getPageSize() > 0) {
            sb.append("&amp;pageSize=");
            sb.append(this.getPageSize());
        }

        if (StringUtils.isNotBlank(this.getSortOrder())) {
            sb.append("&amp;sortOrder=");
            sb.append(this.getSortOrder());
        }

        if (StringUtils.isNotBlank(this.getSortDirection())) {
            sb.append("&amp;sortDirection=");
            sb.append(this.getSortDirection());
        }

        if (StringUtils.isNotBlank(this.getOpenerCallback())) {
            sb.append("&amp;openerCallback=");
            sb.append(this.getOpenerCallback());
        }

        if (StringUtils.isNotBlank(this.getExtra1())) {
            sb.append("&amp;extra1=");
            sb.append(this.getExtra1());
        }

        if (StringUtils.isNotBlank(this.getExtra2())) {
            sb.append("&amp;extra2=");
            sb.append(this.getExtra2());
        }

        if (StringUtils.isNotBlank(this.getListType())) {
            sb.append("&amp;listType=");
            sb.append(this.getListType());
        }

        return sb.toString();
    }

    private void fixBrokenSv(String fromCharSet, String toCharSet) {
        if (StringUtils.isNotBlank(this.getSv())) {
            try {
                this.setSv(new String(this.getSv().getBytes(fromCharSet), toCharSet));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public static String[] BLACKLIST = {"--", ";--", ";", "/*", "*/", "@@", "@", "\"", "'",
            "char", "nchar", "varchar", "nvarchar",
            "alter", "begin", "cast", "create", "cursor", "declare", "delete", "drop", "end",
            "exec", "execute", "fetch", "insert", "kill", "open",
            "select", "sys", "sysobjects", "syscolumns",
            "table", "update"};

    public void fixBrokenSvByDefaultCharsets() {
        ApplicationContext context = ApplicationContextProvider.getApplicationContext();
        ConfigOptionService configService = context.getBean(ConfigOptionService.class);
        Config config = configService.getConfig("global");
        if ("true".equalsIgnoreCase(config.getOption("fix_kor_active"))) {
            this.fixBrokenSv(StringUtils.defaultString(config.getOption("fix_kor_from"), "8859_1"), StringUtils.defaultString(config.getOption("fix_kor_to"), "UTF-8"));
        }
        if (StringUtils.isNotBlank(this.getSortOrder())) {
            for (String black : BLACKLIST) {
                if (this.getSortOrder().contains(black)) {
                    this.setSortOrder(null);
                    break;
                }
            }
        }
        if (StringUtils.isNotBlank(this.getSortDirection())) {
            for (String black : BLACKLIST) {
                if (this.getSortDirection().contains(black)) {
                    this.setSortDirection(null);
                    break;
                }
            }
        }
    }

    public void setDefaultSort(String sortOrder, String sortDirection) {
        if (StringUtils.isBlank(this.getSortOrder())) {
            if (StringUtils.isNotBlank(sortOrder)) {
                this.setSortOrder(sortOrder);
            }
        }
        if (StringUtils.isBlank(this.getSortDirection())) {
            if (StringUtils.isNotBlank(sortDirection)) {
                this.setSortDirection(sortDirection);
            }
        }
    }
}

package egovframework.com.cms.support.Pagination;

import egovframework.com.cms.config.model.Config;
import egovframework.com.cms.config.service.ConfigService;
import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.support.ApplicationContextProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

public class PagingSearch extends MultiSiteVO {
    protected String sc;
    protected String sv;

    protected String pageParamName = "cp"; // 페이지 파라미터명
    protected Integer cp; // 현재 페이지
    protected Integer pageSize = 10;// 한 페이지당 개수
    protected int offset; // MySQL용
    protected int startRow; // Oracle용
    protected int endRow; // Oracle용
    protected boolean paging = true;

    protected String sortOrder;
    protected String sortDirection;
    protected String sortOverride;

    private String openerCallback;
    private String extra1; // 임시 파라미터
    private String extra2;

    private String listType = "list";

    public void setSitePrefix(HttpServletRequest request) {
        Site site = (Site) request.getAttribute("currentSite");
        if (site != null) {
            this.sitePrefix = site.getSitePrefix();
        }
    }

    public String getSc() {
        return sc;
    }

    public void setSc(String sc) {
        this.sc = sc;
    }

    public String getSv() {
        return sv;
    }

    public void setSv(String sv) {
        this.sv = sv;
    }

    public Integer getCp() {
        return cp == null || cp == 0 ? 1 : cp;
    }

    public void setCp(Integer cp) {
        this.cp = cp;
    }

    public int getOffset() {
        if (this.getCp() == 1) {
            return 0;
        } else {
            return this.getPageSize() * (this.getCp() - 1);
        }
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Integer getPageSize() {
        // return pageSize == 0 || pageSize == null ? 10 : pageSize;
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getPageParamName() {
        return pageParamName;
    }

    public void setPageParamName(String pageParamName) {
        this.pageParamName = pageParamName;
    }

    public int getStartRow() {
        //return this.startRow;
        return (this.getCp() - 1) * this.getPageSize() + 1;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getEndRow() {
        //페이징없이 전체 다 가져오도록
        if (!this.isPaging()) {
            return Integer.MAX_VALUE;
        }
        //return endRow;
        return this.getStartRow() + this.getPageSize() - 1;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public boolean isPaging() {
        return paging;
    }

    public void setPaging(boolean paging) {
        this.paging = paging;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getOpenerCallback() {
        return openerCallback;
    }

    public void setOpenerCallback(String openerCallback) {
        this.openerCallback = openerCallback;
    }

    public String getExtra1() {
        return extra1;
    }

    public void setExtra1(String extra1) {
        this.extra1 = extra1;
    }

    public String getExtra2() {
        return extra2;
    }

    public void setExtra2(String extra2) {
        this.extra2 = extra2;
    }

    public String getSortOverride() {
        return sortOverride;
    }

    public void setSortOverride(String sortOverride) {
        this.sortOverride = sortOverride;
    }

    public String getListType() {
        return listType;
    }

    public void setListType(String listType) {
        this.listType = listType;
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

    public void fixBrokenSvByDefaultCharsets() throws Exception {
        ApplicationContext context = ApplicationContextProvider.getApplicationContext();
        ConfigService configService = context.getBean(ConfigService.class);
        Config config = configService.findConfigById("global");
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

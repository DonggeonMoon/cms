package egovframework.com.cms.support.Pagination;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Getter
@Setter
public class Paging {
    private int startPage;
    private int endPage;
    private int currentPage;
    private int prevPage;
    private int nextPage;
    private int totalPage;
    private int rowTop;
    private int rowTotal;
    private int pageSize;
    private int blockSize;
    private String userTextTag;
    private String userImageTag;
    private String adminTextTag;
    private String adminImageTag;
    private String pageParamName;
    private List<?> result;

    private String firstText;
    private String prevText;
    private String nextText;
    private String lastText;

    private String queryString;

    public Paging(List<?> result, int rowTotal, PagingSearch pagingSearch) {
        this(result, rowTotal, pagingSearch.getPageSize(), pagingSearch.getCp(), pagingSearch.getQueryString(), pagingSearch.getPageParamName());
    }

    public Paging(List<?> result, int rowTotal, int pageSize, int currentPage, String queryString, String pageParamName) {
        this(result, rowTotal, pageSize, currentPage, queryString, pageParamName, null, null, null, null);
    }

    public Paging(List<?> result, int rowTotal, int pageSize, int currentPage, String queryString, String pageParamName, int blockSize) {
        this(result, rowTotal, pageSize, currentPage, queryString, pageParamName, blockSize, null, null, null, null);
    }

    public Paging(List<?> result, int rowTotal, int pageSize, int currentPage, String queryString, String pageParamName, String firstText, String prevText, String nextText, String lastText) {
        this(result, rowTotal, pageSize, currentPage, queryString, pageParamName, 10, firstText, prevText, nextText, lastText);
    }

    public Paging(List<?> result, int rowTotal, int pageSize, int currentPage, String queryString, String pageParamName, int blockSize, String firstText, String prevText, String nextText, String lastText) {
        this.pageParamName = pageParamName;
        this.result = result;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPage = 0;
        this.rowTotal = rowTotal;
        this.queryString = queryString;

        this.firstText = firstText;
        this.prevText = prevText;
        this.nextText = nextText;
        this.lastText = lastText;

        if ((double) this.rowTotal / (double) this.pageSize <= 1) {
            totalPage = 1;
        } else {
            totalPage = (int) Math.ceil((double) rowTotal / (double) this.pageSize);
        }
        this.blockSize = blockSize;

        rowTop = rowTotal - (currentPage - 1) * pageSize;

        startPage = ((currentPage - 1) / blockSize) * blockSize + 1;
        //startPage = startPage - 1 < 1 ? 1 : startPage - 1;
        startPage = startPage - 1 < 1 ? 1 : startPage;
        endPage = startPage + blockSize - 1;

        if (endPage > totalPage) {
            endPage = totalPage;
        }

        prevPage = startPage == 1 ? 1 : startPage - 1;
        //nextPage = startPage == 1 ? blockSize + 1 : ( startPage + blockSize >= totalPage ? totalPage : startPage + blockSize );
        nextPage = endPage >= totalPage ? endPage : endPage + 1;

        if ("?".equals(this.queryString)) {
            this.queryString = "";
        }

        userTextTag = buildUserTextTag(this.queryString);
        userImageTag = buildUserImageTag(this.queryString);
        adminTextTag = buildAdminTextTag(this.queryString);
        adminImageTag = buildAdminImageTag(this.queryString);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public int getRowTop() {
        return rowTop;
    }

    public int getRowTotal() {
        return rowTotal;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public String getUserTextTag() {
        return userTextTag;
    }

    public void setUserTextTag(String userTextTag) {
        this.userTextTag = userTextTag;
    }

    public String getUserImageTag() {
        return userImageTag;
    }

    public void setUserImageTag(String userImageTag) {
        this.userImageTag = userImageTag;
    }

    public String getAdminTextTag() {
        return adminTextTag;
    }

    public void setAdminTextTag(String adminTextTag) {
        this.adminTextTag = adminTextTag;
    }

    public String getAdminImageTag() {
        return adminImageTag;
    }

    public void setAdminImageTag(String adminImageTag) {
        this.adminImageTag = adminImageTag;
    }

    public List<?> getResult() {
        return result;
    }

    public String getQueryString() {
        return queryString;
    }

    private String buildUserTextTag(String qs) {
        String firstText = StringUtils.isBlank(this.firstText) ? "&lt;&lt;" : this.firstText;
        String prevText = StringUtils.isBlank(this.prevText) ? "&lt;" : this.prevText;
        String nextText = StringUtils.isBlank(this.nextText) ? "&gt;" : this.nextText;
        String lastText = StringUtils.isBlank(this.lastText) ? "&gt;&gt;" : this.lastText;

        String tag = "<ul class=\"pagination\">";
        tag += "<li><a href=\"?" + pageParamName + "=1" + qs + "\" class=\"page_first\" title=\"첫 페이지\"><span>" + firstText + "</span></a></li>";
        tag += "<li><a href=\"?" + pageParamName + "=" + this.prevPage + qs + "\" class=\"page_prev\" title=\"이전 페이지\"><span>" + prevText + "</span></a></li>";
        for (int i = startPage; i < (endPage + 1); i++) {
            if (currentPage == i) {
                tag += "<li class=\"active\"><a href=\"?" + pageParamName + "=" + i + qs + "\" class=\"page_current\" title=\"현재 페이지\"><span>" + i + "</span></a></li>";
            } else {
                tag += "<li><a href=\"?" + pageParamName + "=" + i + qs + "\" class=\"page_num\"><span>" + i + "</span></a></li>";
            }
        }
        tag += "<li><a href=\"?" + pageParamName + "=" + this.nextPage + qs + "\" class=\"page_next\" title=\"다음 페이지\"><span>" + nextText + "</span></a></li>";
        tag += "<li><a href=\"?" + pageParamName + "=" + this.totalPage + qs + "\" class=\"page_last\" title=\"마지막 페이지\"><span>" + lastText + "</span></a></li>";

        return tag + "</ul>";
    }

    private String buildUserImageTag(String qs) {
        String firstText = StringUtils.isBlank(this.firstText) ? "&lt;&lt;" : this.firstText;
        String prevText = StringUtils.isBlank(this.prevText) ? "&lt;" : this.prevText;
        String nextText = StringUtils.isBlank(this.nextText) ? "&gt;" : this.nextText;
        String lastText = StringUtils.isBlank(this.lastText) ? "&gt;&gt;" : this.lastText;

        String tag = "<ul class=\"pagination\">";
        tag += "<li><a href=\"?" + pageParamName + "=1" + qs + "\" class=\"page_first\" title=\"첫 페이지\"><span>" + firstText + "</span></a></li>";
        tag += "<li><a href=\"?" + pageParamName + "=" + this.prevPage + qs + "\" class=\"page_prev\" title=\"이전 페이지\"><span>" + prevText + "</span></a></li>";
        for (int i = startPage; i < (endPage + 1); i++) {
            if (currentPage == i) {
                tag += "<li class=\"active\"><a href=\"?" + pageParamName + "=" + i + qs + "\" class=\"page_current\" title=\"" + i + " 페이지\"><span>" + i + "</span></a></li>";
            } else {
                tag += "<li><a href=\"?" + pageParamName + "=" + i + qs + "\" class=\"page_num\" title=\"" + i + " 페이지\"><span>" + i + "</span></a></li>";
            }
        }
        tag += "<li><a href=\"?" + pageParamName + "=" + this.nextPage + qs + "\" class=\"page_next\" title=\"다음 페이지\"><span>" + nextText + "</span></a></li>";
        tag += "<li><a href=\"?" + pageParamName + "=" + this.totalPage + qs + "\" class=\"page_last\" title=\"마지막 페이지\"><span>" + lastText + "</span></a></li>";

        return tag + "</ul>";
    }

    private String buildAdminTextTag(String qs) {
        String firstText = StringUtils.isBlank(this.firstText) ? "&lt;&lt;" : this.firstText;
        String prevText = StringUtils.isBlank(this.prevText) ? "&lt;" : this.prevText;
        String nextText = StringUtils.isBlank(this.nextText) ? "&gt;" : this.nextText;
        String lastText = StringUtils.isBlank(this.lastText) ? "&gt;&gt;" : this.lastText;

        String tag = "<ul>";
        tag += "<li><a href=\"?" + pageParamName + "=1" + qs + "\" class=\"page_first\">" + firstText + "</a></li>";
        tag += "<li><a href=\"?" + pageParamName + "=" + this.prevPage + qs + "\" class=\"page_prev\">" + prevText + "</a></li>";
        for (int i = startPage; i < (endPage + 1); i++) {
            if (currentPage == i) {
                tag += "<li class=\"active\"><a href=\"?" + pageParamName + "=" + i + qs + "\" class=\"page_current\">" + i + "</a></li>";
            } else {
                tag += "<li><a href=\"?" + pageParamName + "=" + i + qs + "\" class=\"page_num\">" + i + "</a></li>";
            }
        }
        tag += "<li><a href=\"?" + pageParamName + "=" + this.nextPage + qs + "\" class=\"page_next\">" + nextText + "</a></li>";
        tag += "<li><a href=\"?" + pageParamName + "=" + this.totalPage + qs + "\" class=\"page_last\">" + lastText + "</a></li>";

        return tag + "</ul>";
    }

    private String buildAdminImageTag(String qs) {
        String firstText = StringUtils.isBlank(this.firstText) ? "&lt;&lt;" : this.firstText;
        String prevText = StringUtils.isBlank(this.prevText) ? "&lt;" : this.prevText;
        String nextText = StringUtils.isBlank(this.nextText) ? "&gt;" : this.nextText;
        String lastText = StringUtils.isBlank(this.lastText) ? "&gt;&gt;" : this.lastText;

        String tag = "<ul>";
        tag += "<li><a href=\"?" + pageParamName + "=1" + qs + "\" class=\"page_first\">" + firstText + "</a></li>";
        tag += "<li><a href=\"?" + pageParamName + "=" + this.prevPage + qs + "\" class=\"page_prev\">" + prevText + "</a></li>";
        for (int i = startPage; i < (endPage + 1); i++) {
            if (currentPage == i) {
                tag += "<li class=\"active\"><a href=\"?" + pageParamName + "=" + i + qs + "\" class=\"page_current\">" + i + "</a></li>";
            } else {
                tag += "<li><a href=\"?" + pageParamName + "=" + i + qs + "\" class=\"page_num\">" + i + "</a></li>";
            }
        }
        tag += "<li><a href=\"?" + pageParamName + "=" + this.nextPage + qs + "\" class=\"page_next\">" + nextText + "</a></li>";
        tag += "<li><a href=\"?" + pageParamName + "=" + this.totalPage + qs + "\" class=\"page_last\">" + lastText + "</a></li>";

        return tag + "</ul>";
    }
}

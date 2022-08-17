package egovframework.com.cms.statistics.dto;

import com.ibm.icu.text.SimpleDateFormat;
import egovframework.com.cms.member.dto.Member;
import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.support.exception.EcmsException;
import egovframework.com.cms.support.util.Utils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Locale;

@NoArgsConstructor
@Getter
@Setter
public class StatAccessLog extends MultiSiteVO {
    private Integer logId;
    private String logRequestUri;
    private String logRequestMethod;
    private String memberId;
    private String logRemoteIp;
    private String logReferer;
    private Date logRegDate;

    private String logTableName;

    public StatAccessLog(HttpServletRequest request) {
        this(request, null);
    }

    public StatAccessLog(HttpServletRequest request, Site currentSite) {
        if (currentSite == null) {
            currentSite = (Site) request.getAttribute("currentSite");
            if (currentSite == null) {
                throw new EcmsException("current menu should not be null!");
            }
        }
        this.logRequestUri = StringUtils.abbreviate(Utils.getDecodedIfEncoded(Utils.getSchemeDomainPort(request) + Utils.getRequestUriWithParameters(request)), 990);
        this.logRequestMethod = request.getMethod().toUpperCase();
        Member currentUser = (Member) request.getSession().getAttribute("currentUser");
        this.memberId = currentUser.getMemberId();
        this.logRemoteIp = StringUtils.abbreviate(Utils.getRemoteIp(request), 990);
        this.logReferer = Utils.getDecodedIfEncoded(StringUtils.defaultString(request.getHeader("referer"), ""));
        this.logRegDate = new Date();
        this.logTableName = currentSite.getSitePrefix() + "_STAT_LOG_" + new SimpleDateFormat("yyyyMM", Locale.KOREA).format(this.getLogRegDate());
    }
}

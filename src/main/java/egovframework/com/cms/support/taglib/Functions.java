package egovframework.com.cms.support.taglib;

import egovframework.com.cms.code.model.Code;
import egovframework.com.cms.index.dto.SEO;
import egovframework.com.cms.member.model.Member;
import egovframework.com.cms.role.service.RoleService;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.support.ApplicationContextProvider;
import egovframework.com.cms.support.CryptoWorker;
import egovframework.com.cms.support.util.Utils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import javax.servlet.jsp.PageContext;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Functions {
    public static String nl2br(String value, boolean escapeHtml) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        if (escapeHtml) {
            value = StringEscapeUtils.escapeHtml4(value);
        }
        return Utils.nl2br(value);
    }

    public static String abbreviate(String string, int length) {
        if (StringUtils.isBlank(string)) {
            return "";
        }
        if (length <= 4) {
            return string;
        }
        String temp;
        temp = StringUtils.abbreviate(string, length);
        return temp;
    }

    public static String urlEncode(String src, String encoding) {
        try {
            return URLEncoder.encode(src, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return src;
        }
    }

    public static String urlDecode(String src, String encoding) {
        try {
            return URLDecoder.decode(src, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return src;
        }
    }

    public static String encrypt(String str) {
        ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
        CryptoWorker cryptoWorker = ctx.getBean(CryptoWorker.class);
        return cryptoWorker.encrypt(str);
    }

    public static String decrypt(String str) {
        ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
        CryptoWorker cryptoWorker = ctx.getBean(CryptoWorker.class);
        return cryptoWorker.decrypt(str);
    }

    public static String printAdminMenuLink(Site currentSite, Member currentUser, String adminMenuName, String adminMenuLink, String method, String className) {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        RoleService roleService = applicationContext.getBean(RoleService.class);

        adminMenuLink = Utils.getAdminPath(currentSite) + adminMenuLink;
        String link = "";
        if (currentSite == null || currentUser == null) {
            return link;
        }
        String[] temp = adminMenuLink.split("\\?");
        Map<String, String[]> paramMap = null;
        if (temp.length > 1) {
            String qs = temp[1];
            paramMap = Utils.parseQueryString(qs);
        }
        if (roleService.isCurrentUserAccessibleResource(currentUser, temp[0], paramMap, method)) {
            link = "<li class=\"" + className + "\"><a target=\"frame_main\" href=\"" + adminMenuLink + "\">" + adminMenuName + "</a></li>";
        }
        return link;
    }

    public static String codeName(String codeId, List<Code> codeList) {
        if (StringUtils.isBlank(codeId) || codeList == null) {
            return codeId;
        }
        for (Code code : codeList) {
            if (codeId.equals(code.getCodeId())) {
                return code.getCodeName();
            }
        }
        return codeId;
    }

    public static String codeNameEn(String codeId, List<Code> codeList) {
        if (StringUtils.isBlank(codeId) || codeList == null) {
            return codeId;
        }
        for (Code code : codeList) {
            if (codeId.equals(code.getCodeId())) {
                return code.getCodeNameEn();
            }
        }
        return codeId;
    }

    @SuppressWarnings("unchecked")
    public static String catSubCodeName(String pgCatCode, String prefix, String codeId, String suffix, PageContext pageContext) {
        List<Code> codeList = (List<Code>) pageContext.getRequest().getAttribute(prefix + pgCatCode + suffix);
        if (StringUtils.isBlank(codeId) || codeList == null) {
            return codeId;
        }
        for (Code code : codeList) {
            if (codeId.equals(code.getCodeId())) {
                return code.getCodeName();
            }
        }
        return codeId;
    }

    public static String formatStringDate(String dateString, String parseFormat, String newFormat) {
        if (StringUtils.isBlank(dateString) || StringUtils.isBlank(parseFormat) || StringUtils.isBlank(newFormat)) {
            return dateString;
        }
        SimpleDateFormat parseFmt = new SimpleDateFormat(parseFormat);
        try {
            Date date = parseFmt.parse(dateString);
            return new SimpleDateFormat(newFormat).format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }

    public static String seoLocation(SEO seo, String divider, String cssClass) {
        if (seo == null) {
            return "";
        }
        return seo.getLocation(divider, cssClass);
    }
}

package egovframework.com.cms.support.util;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.ChineseCalendar;
import egovframework.com.cms.config.model.Config;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.support.Constant;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.util.WebUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class Utils {
    public static String getRempoteIp(HttpServletRequest request) {
        String ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private static final String EMAIL_PATTERN = "(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)";

    public static boolean isEmail(String emailStr) {

        if (StringUtils.containsWhitespace(emailStr)) {
            return false;
        }

        //한글포함되어 있으면 안됨..한글 이메일 주소도 있다고는 하는데 일단 막음 -_-
        if (hasKoreanChars(emailStr)) {
            return false;
        }

        Pattern p = Pattern.compile(EMAIL_PATTERN);
        return p.matcher(emailStr).matches();
    }

    public static String getKoreanFileNameToSave(HttpServletRequest request, String fileKoreanName) {
        //User Agent
        //UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        UserAgent userAgent = (UserAgent) request.getAttribute("userAgent");
        //log.info("##### UserAgent Group : {}, Name : {}, VERSION: {}", new Object[]{userAgent.getBrowser().getGroup(), userAgent.getBrowser(), userAgent.getBrowserVersion()});
        String fileNameToSave = null;

        try {
            if (userAgent.getBrowser().getGroup().equals(Browser.IE)) {
                fileNameToSave = URLEncoder.encode(fileKoreanName, "UTF8");
            } else if (userAgent.getBrowser().getGroup().equals(Browser.FIREFOX)) {
                fileNameToSave = new String(fileKoreanName.getBytes("UTF-8"), "8859_1");
            } else if (userAgent.getBrowser().getGroup().equals(Browser.CHROME)) {
                fileNameToSave = new String(fileKoreanName.getBytes("UTF-8"), "8859_1");
            } else if (userAgent.getBrowser().getGroup().equals(Browser.SAFARI)) {
                fileNameToSave = new String(fileKoreanName.getBytes("UTF-8"), "8859_1");
            } else {
                fileNameToSave = new String(fileKoreanName.getBytes("UTF-8"), "8859_1");
            }
            fileNameToSave = fileNameToSave.replaceAll("\\+", " ");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return fileNameToSave;
    }

    public static final int MAP_SORT_ASC = 100;
    public static final int MAP_SORT_DESC = 200;

    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map, int sortDirection) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        if (sortDirection == MAP_SORT_DESC) {
            Collections.reverse(list);
        }

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static Date makeNoon(Date dateParam) {

        Calendar cal = Calendar.getInstance(Locale.KOREA);
        cal.setTime(dateParam);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();

        return date;
    }

    public static Calendar makeNoon(Calendar cal) {

        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }

    public static int countAbsDaysOfTwoDays(Date date1Param, Date date2Param) {
        Date date1 = makeNoon(date1Param);
        Date date2 = makeNoon(date2Param);
        return (int) (Math.abs((date1.getTime() - date2.getTime())) / (1000 * 60 * 60 * 24f));
    }

    public static BigDecimal floored(BigDecimal bigDecimal, int targetLevel) {
        return new BigDecimal(String.valueOf(bigDecimal.longValue() / 10 * 10));
    }

    public static String getFileSizeString(long value, boolean useSuffix, boolean capitalizeSuffix) {
        long absValue = Math.abs(value);
        String suffix = "";
        double result = value;
        if (value < 1024) {
            suffix = "bytes";
        } else if (absValue < 1024 * 1024) {
            result = value / 1024.0;
            suffix = "kb";
        } else if (absValue < 1024 * 1024 * 1024) {
            result = value / (1024.0 * 1024);
            suffix = "mb";
        } else {
            result = value / (1024.0 * 1024 * 1024);
            suffix = "gb";
        }
        if (useSuffix) {
            suffix = capitalizeSuffix ? suffix.toUpperCase() : suffix;
        } else {
            suffix = "";
        }
        return new DecimalFormat("0.0").format(result) + suffix;
    }

    public static String getJsoupFilteredText(String src, Whitelist jsoupWhiteList, boolean keepCRLF, boolean treatBRAsCRLF) {
        if (StringUtils.isBlank(src)) {
            return src;
        }
        if (jsoupWhiteList == null) {
            jsoupWhiteList = Whitelist.none();
        }
        String string = src;
        if (keepCRLF) {
            string = string.replace("\r\n", "_CR_LF_");
            string = string.replace("\r", "_CR_");
            string = string.replace("\n", "_LF_");
            if (treatBRAsCRLF) {
                string.replaceAll("/(<br\\s?\\/?>)+/gi", "_CR_LF_");
            }
            string = Jsoup.clean(string, jsoupWhiteList);
            string = string.replace("_CR_LF_", "\r\n");
            string = string.replace("_CR_", "\r");
            string = string.replace("_LF_", "\n");

            return string;
        } else {
            return Jsoup.clean(string, jsoupWhiteList);
        }
    }

    public static String getFixedRequestUri(HttpServletRequest request) {
        String temp = request.getRequestURI();
        if (temp.startsWith("/WEB-INF/") && temp.endsWith(".jsp")) {
            temp = (String) request.getAttribute("javax.servlet.forward.request_uri");
        }
        //톰캣 jsessionid
        if (temp.contains(";")) {
            temp = temp.split(";")[0];
        }
        return StringUtils.removeEnd(temp, "/");
    }

    public static String getCurrentUrl(HttpServletRequest request, boolean prependDomain) {
        return getCurrentUrl(request, prependDomain, false);
    }

    public static String getCurrentUrl(HttpServletRequest request, boolean prependDomain, boolean encode) {
        StringBuilder sb = new StringBuilder();

        if (prependDomain) {
            sb.append(getSchemeDomainPort(request));
        }

        String fixedRequestUri = getFixedRequestUri(request);
        sb.append(fixedRequestUri);

        String result = null;

        if (fixedRequestUri.contains("/member/public/login") && StringUtils.isNotBlank(request.getParameter("returnUrl"))) {
            result = request.getParameter("returnUrl");
        }
        //기본처리
        else {
            if (StringUtils.isNotBlank(request.getQueryString())) {
                sb.append("?");
                sb.append(request.getQueryString());
            }
            result = sb.toString();
        }

        if (encode) {
            try {
                result = URLEncoder.encode(result, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String getRequestUriWithParameters(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getQueryString())) {
            return getFixedRequestUri(request) + "?" + request.getQueryString();
        } else {
            return getFixedRequestUri(request);
        }
    }

    public static String getSchemeDomainPort(HttpServletRequest request) {
        return getSchemeDomainPort(request, null, null);
    }

    public static String getSchemeDomainPort(HttpServletRequest request, String scheme, String port) {
        StringBuilder sb = new StringBuilder(30);
        if (StringUtils.isNotBlank(scheme)) {
            sb.append(scheme);
        } else {
            sb.append(request.getScheme());
            scheme = request.getScheme();
        }
        sb.append("://");
        sb.append(request.getServerName());
        if (StringUtils.isNotBlank(port)) {
            if (!"80".equals(port) && !"443".equals(port)) {
                sb.append(":");
                sb.append(port);
            }
        } else {
            if ("https".equals(scheme)) {
                if (request.getServerPort() != 443) {
                    sb.append(":");
                    sb.append(request.getServerPort());
                }
                //if( StringUtils.isBlank(port) ){
                //	sb.append(":443");
                //}
            } else {
                if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                    sb.append(":");
                    sb.append(request.getServerPort());
                }
            }
        }
        return sb.toString();
    }

    public static String getAllFieldErrorMessage(BindingResult bindingResult, String glue) {
        List<FieldError> errorList = bindingResult.getFieldErrors();
        StringBuilder sb = new StringBuilder(100);
        FieldError fieldError = null;
        for (int i = 0; i < errorList.size(); i++) {
            fieldError = errorList.get(i);
            if (i > 0) {
                sb.append(glue);
            }
            sb.append(fieldError.getDefaultMessage());
        }
        return sb.toString();
    }

    private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public static boolean isIPAdress(String string) {
        Pattern p = Pattern.compile(IPADDRESS_PATTERN);
        return p.matcher(string).matches();
    }

    public static Cookie getCookie(Cookie[] cookies, String cookieName) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        deleteCookie(request, response, cookieName, "/");
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String path) {
        Cookie cookie = new Cookie(cookieName, null);
        Config globalConfig = (Config) request.getAttribute("globalConfig");
        if (globalConfig != null) {
            cookie.setDomain(globalConfig.getOption("cookie_domain"));
        } else {
            log.info("[ECMS] EcmsUtils global config is null. maybe sub sirectory site request?");
        }
        cookie.setPath(path);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    public static final int LUNAR_LAST_DAY_OF_MONTH = 32;

    public static Calendar fromLunarCalToSolarCal(int lunarYear, int lunarMonth, int lunarDay) {

        //구정 12월 31일은 전년도로 만들어줘야 해서...이렇게 하는게 맞게하는건지 알수 없음.
        if (lunarMonth == 12 && lunarDay == LUNAR_LAST_DAY_OF_MONTH) {
            lunarYear -= 1;
        }
        ChineseCalendar chineseCalendar = new ChineseCalendar();
        chineseCalendar.set(ChineseCalendar.EXTENDED_YEAR, lunarYear + 2637);
        chineseCalendar.set(ChineseCalendar.MONTH, lunarMonth - 1);
        //구정 12월 31일은 전년도로 만들어줘야 해서...이렇게 하는게 맞게하는건지 알수 없음.
        //음력으로 31일이라고 썼지만 음력 31이 없을 수도 있으므로 다시 구함
        if (lunarMonth == 12 && lunarDay == LUNAR_LAST_DAY_OF_MONTH) {
            lunarDay = chineseCalendar.getActualMaximum(ChineseCalendar.DAY_OF_MONTH);
        }
        chineseCalendar.set(ChineseCalendar.DAY_OF_MONTH, lunarDay);
        chineseCalendar.set(ChineseCalendar.IS_LEAP_MONTH, 0);
        //음력 -> 양력
        Calendar solarCal = Calendar.getInstance();
        solarCal.setTimeInMillis(chineseCalendar.getTimeInMillis());

        return solarCal;
    }

    public static String string2Slug(String str) {
        return string2Slug(str, true);
    }

    public static String string2Slug(String str, boolean unCapitalize) {
        //extract text
        str = Jsoup.parse(str).text();
        // 마지막에 있는 . 제거
        str = StringUtils.removeEnd(str, ".");
        // |“|”, |’|‘=> 똑같은거 두개 들어간거 아니니까 하나 지우지 말것. 두개 다른거임.
        str = str.replaceAll("\\+|;|\\?|\"|'|“|”|’|‘|\\.|『|』|「|」|`|\\!|#|\\$|%|\\^|\\*|@|\\||~|_|［|］", "");
        //공백...등 - 로 치환
        str = str.replaceAll("\\s+|&|=|,|\\[|\\]|\\{|\\}|\\<|\\>|:|\\(|\\)|…|/|：|:|;", "-");
        //--- 같은거 - 로 치환
        str = str.replaceAll("-+|·+", "-");
        //앞에 붙은 - 제거
        str = StringUtils.removeStart(str, "-");
        //뒤에 붙은 - 제거
        str = StringUtils.removeEnd(str, "-");
        if (unCapitalize) {
            //영문은 다 소문자로
            str = str.toLowerCase();
        }
        return str;
    }

    public static String string2TagName(String str) {
        return string2Slug(str, false).replace("-", " ");
    }

    public static void writeJson(HttpServletResponse response, Object object) {
        ObjectMapper mapper = new ObjectMapper();
        response.setContentType("application/json; charset=UTF-8");
        try {
            mapper.writeValue(response.getWriter(), object);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String toJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(object);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static String getDecodedIfEncoded(String str) {
        if (str.contains("%")) {
            try {
                str = URLDecoder.decode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    public static String fixKorParameter(String string, Config config) {
        if (StringUtils.isNotBlank(string)) {
            try {
                if ("true".equals(config.getOption("fix_kor_active"))) {
                    return new String(string.getBytes(config.getOption("fix_kor_from")), config.getOption("fix_kor_to"));
                } else {
                    return string;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String getThemePath(HttpServletRequest request) {
        return "theme/" + request.getAttribute("theme") + "/";
    }

    public static String getSiteUploadDirectory(HttpServletRequest request) {
        Site currentSite = (Site) request.getAttribute("currentSite");
        String webRoot = null;
        webRoot = getWebRoot(request);
        webRoot = FilenameUtils.getFullPathNoEndSeparator(webRoot).replaceAll("\\\\", "/");
        return webRoot + Constant.UPLOAD_PATH + "/" + currentSite.getSiteId();
    }

    public static String nl2br(String html) {
        //String str = html.replace("\r\n", "<br />");
        //str = str.replace("\r", "<br />");
        //str = str.replace("\n", "<br />");
        String value = html
                .replace("\r\n", "<br />")
                .replace("\r", "<br />")
                .replace("\n", "<br />");
        //value = value.replaceAll("(\\s*<br \\/>\\s*)+", "<br />");
        value = value
                .replaceAll("(\\s*<br \\/>\\s*){3,}", "<br /><br />");//br 3개이상 연속 나오는건 2개로 바꿈

        return value;
    }

    public static String getSlugFromRequestUri(String requestUri) {
        // ${APP_PATH}/archive/post/2015-세계산불총회-종합계획-수립-용역-입찰공고
        String[] temps = requestUri.split("/");
        int size = temps.length;
        String slug = temps[size - 1];
        try {
            slug = URLDecoder.decode(slug, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return slug;
    }

    public static void removeFromSessionStartsWith(HttpServletRequest request, String startsWith) {
        @SuppressWarnings("rawtypes")
        Enumeration enums = request.getSession().getAttributeNames();
        while (enums.hasMoreElements()) {
            String key = (String) enums.nextElement();
            if (key.startsWith(startsWith)) {
                request.getSession().removeAttribute(key);
            }
        }
    }

    public static String getWebRoot(HttpServletRequest request) {
        String webRoot = null;
        try {
            webRoot = WebUtils.getRealPath(request.getSession().getServletContext(), "/");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return webRoot;
    }

    public static void guessEncoding(String str, String... encodings) {
        if (StringUtils.isBlank(str)) {
            System.out.println("EcmsUtils : str is null.");
        }
        try {
            System.out.println("EcmsUtils : START SHALLOW MODE ============================");
            for (String getBytesEnc : encodings) {
                for (String newStringEnc : encodings) {
                    System.out.println("EcmsUtils guessEncoding SHALLOW! : " + getBytesEnc + ",	" + newStringEnc + " = " + new String(str.getBytes(getBytesEnc), newStringEnc));
                }
            }
			/*
			System.out.println("EcmsUtils : START DEEP MODE ============================");
			for( String getBytesEnc : encodings ){
				for( String newStringEnc : encodings ){
					for( String getBytesEncDeep : encodings ){
						for( String newStringEncDeep : encodings ){
							System.out.println("EcmsUtils guessEncoding DEEP! : " + getBytesEnc + ", " + newStringEnc + ", " + getBytesEncDeep + ", " + newStringEncDeep + " = " + new String(str.getBytes(getBytesEnc), newStringEnc));
						}
					}
				}
			}
			*/
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void guessEncoding(String str) {
        guessEncoding(str, "EUC-KR", "8859_1", "UTF-8");
    }

    public static boolean hasKoreanChars(String string) {
        if (string == null || "".equals(string)) {
            return false;
        }
        return string.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
    }

    public static String getXmlData(String filePath, String xPathExpression) {
        String data = null;
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(filePath);
            XPath xPath = XPathFactory.newInstance().newXPath();
            data = xPath.evaluate(xPathExpression, document);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static String getXmlData(Document document, String xPathExpression) {
        String data = null;
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            data = xPath.evaluate(xPathExpression, document);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static String getAppPath(Site site) {
        return Constant.WAS_PATH + Constant.APP_PATH + "/" + (site.isSiteMain() ? Constant.MAIN_SITE_DISPLAY_ID : site.getSiteId());
    }

    public static String getAppPath(String prefix, Site site) {
        return prefix + Constant.APP_PATH + "/" + (site.isSiteMain() ? Constant.MAIN_SITE_DISPLAY_ID : site.getSiteId());
    }


    public static String getAdminPath(Site site) {
        return getAppPath(site) + Constant.ADMIN_PATH;
    }

    public static String getAdminPath(String prefix, Site site) {
        return getAppPath(prefix, site) + Constant.ADMIN_PATH;
    }

    public static boolean isAdminPath(String requestUri) {
        return requestUri.contains(Constant.ADMIN_PATH + "/") && requestUri.endsWith(".do");
    }

    public static boolean isUserPath(String requestUri) {
        return requestUri.startsWith(Constant.WAS_PATH + Constant.APP_PATH + "/") && !requestUri.endsWith(".do");
    }

    public static boolean isApiPath(String requestUri) {
        return requestUri.contains(Constant.API_PATH + "/");
    }

    public static boolean isFilePath(String requestUri) {
        return requestUri.contains(Constant.FILE_PATH + "/");
    }

    public static boolean isFeedPath(String requestUri) {
        return requestUri.contains(Constant.FEED_PATH + "/");
    }

    public static boolean isDevPath(String serverName) {
        return serverName.contains("localhost") || serverName.contains("127.0.0.1") || serverName.contains(".dev.com");
    }

    public static boolean isEpartNet(HttpServletRequest request) {
        return request.getServerName().contains(".epart.net") || request.getServerName().contains("110.45.147.184");
    }

    public static Map<String, String[]> parseQueryString(String s) {

        String valArray[] = null;

        if (s == null) {
            throw new IllegalArgumentException();
        }
        Map<String, String[]> hm = new HashMap<String, String[]>();
        StringBuffer sb = new StringBuffer();
        StringTokenizer st = new StringTokenizer(s, "&");
        while (st.hasMoreTokens()) {
            String pair = (String) st.nextToken();
            int pos = pair.indexOf('=');
            if (pos == -1) {
                // XXX
                // should give more detail about the illegal argument
                throw new IllegalArgumentException();
            }
            String key = parseName(pair.substring(0, pos), sb);
            String val = parseName(pair.substring(pos + 1, pair.length()), sb);
            if (hm.containsKey(key)) {
                String oldVals[] = (String[]) hm.get(key);
                valArray = new String[oldVals.length + 1];
                for (int i = 0; i < oldVals.length; i++)
                    valArray[i] = oldVals[i];
                valArray[oldVals.length] = val;
            } else {
                valArray = new String[1];
                valArray[0] = val;
            }
            hm.put(key, valArray);
        }
        return hm;
    }

    private static String parseName(String s, StringBuffer sb) {
        sb.setLength(0);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '+':
                    sb.append(' ');
                    break;
                case '%':
                    try {
                        sb.append((char) Integer.parseInt(s.substring(i + 1, i + 3), 16));
                        i += 2;
                    } catch (NumberFormatException e) {
                        // XXX
                        // need to be more specific about illegal arg
                        throw new IllegalArgumentException();
                    } catch (StringIndexOutOfBoundsException e) {
                        String rest = s.substring(i);
                        sb.append(rest);
                        if (rest.length() == 2)
                            i++;
                    }

                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    public static String getRandomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getLoginUrl(Site currentSite, String returlUrl) {
        if (StringUtils.isBlank(returlUrl)) {
            return getAppPath(currentSite) + "/member/public/login";
        } else {
            try {
                return getAppPath(currentSite) + "/member/public/login?returnUrl=" + URLEncoder.encode(returlUrl, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return getAppPath(currentSite) + "/member/public/login?returnUrl=" + returlUrl;
            }
        }
    }

    public static Date getTimeChangedDate(Date date, int hour24, int min, int sec) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hour24);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, sec);

        return cal.getTime();
    }

    public static String getFormattedDate(Date date) {
        return getFormattedDate(date, "yyyy-MM-dd");
    }

    public static String getFormattedDate(Date date, String format) {
        if (date == null) {
            return null;
        }
        if (StringUtils.isBlank(format)) {
            format = "yyyy-MM-dd";
        }
        return new SimpleDateFormat(format).format(date);
    }

    public static Date getParsedDate(String dateString) {
        return getParsedDate(dateString, "yyyy-MM-dd HH:mm:ss");
    }

    public static Date getParsedDate(String dateString, String pattern) {
        if (StringUtils.isBlank(dateString)) {
            return null;
        }
        if (StringUtils.isBlank(pattern)) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean forceMakeDirectory(String directoryPath) {
        if (StringUtils.isBlank(directoryPath)) {
            return false;
        }
        File directory = new File(directoryPath);
        //이미 있는데 디렉토리가 아니다
        if (directory.exists() && !directory.isDirectory()) {
            System.out.println("[seocho] EcmsUtils : " + directoryPath + " is already exists. But target is not a directory.");
            return false;
        } else if (directory.exists() && directory.isDirectory()) {
            // pass
        } else {
            try {
                FileUtils.forceMkdir(directory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (directory.exists()) {
            if (directory.canRead()) {
                directory.setReadable(true, false);
            }
            if (directory.canWrite()) {
                directory.setWritable(true, false);
            }
            if (directory.canExecute()) {
                directory.setExecutable(true, false);
            }
        }
        return true;
    }

    public static final String makeUUIDFileSaveNameWithExt(String filename) {
        if (StringUtils.isBlank(filename)) {
            return null;
        }
        String ext = FilenameUtils.getExtension(filename);
        String dotExt = "";
        if (!StringUtils.isBlank(ext)) {
            dotExt = "." + ext.toLowerCase();
        }
        return getRandomUUID() + dotExt;
    }

    public static int getFullAge(String birthdayString, String parsePattern) {
        if (StringUtils.isBlank(birthdayString) || StringUtils.isBlank(parsePattern)) {
            return 0;
        }

        try {
            Date birthday = DateUtils.parseDate(birthdayString, parsePattern);

            Calendar todayCal = Calendar.getInstance(Locale.KOREA);
            Date today = todayCal.getTime();
            int todayYear = todayCal.get(Calendar.YEAR);

            Calendar birthCal = Calendar.getInstance(Locale.KOREA);
            birthCal.setTime(birthday);
            int birthYear = birthCal.get(Calendar.YEAR);

            int age = todayYear - birthYear;

            //월일 비교하기위해 년도 맞춰줌
            birthCal.add(Calendar.YEAR, age);
            Date birthday2 = birthCal.getTime();
            //생일 안지남
            if (birthday2.after(today)) {
                age--;
            }

            if (age <= 0) {
                return 0;
            }
            return age;

        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void writeSimpleFileLog(String fileAbsolutePath, String message, String rollingFileInterval) {
        FileWriter fileWriter = null;
        try {
            Date date = new Date();
            if ("day".equals(rollingFileInterval)) {
                String day = new SimpleDateFormat("yyyy-MM-dd").format(date);
                fileWriter = new FileWriter(new File(fileAbsolutePath + "." + day), true);
            } else if ("month".equals(rollingFileInterval)) {
                String month = new SimpleDateFormat("yyyy-MM").format(date);
                fileWriter = new FileWriter(new File(fileAbsolutePath + "." + month), true);
            } else {
                fileWriter = new FileWriter(new File(fileAbsolutePath), true);
            }
            fileWriter.write("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date) + "] ");
            fileWriter.write(message);
            fileWriter.write(System.getProperty("line.separator"));
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getPrefixedFile(String directory, String prefix) {
        File dir = new File(directory);
        //폴더자체가 없다
        if (!dir.exists()) {
            return null;
        }
        List<File> files = (List<File>) FileUtils.listFiles(dir, FileFilterUtils.prefixFileFilter(prefix), null);
        if (files != null && files.size() > 0) {
            if (files.size() == 1) {
                return files.get(0);
            } else {
                // 원래 무조건 하나만 있어야 하는데 하나 이상 있으면 제일 최신거 반환
                Collections.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        if (o1.lastModified() < o2.lastModified()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });

                return files.get(0);
            }
        } else {
            // nothing
            return null;
        }
    }

    public static List<File> getPrefixedFiles(String directory, String prefix) {
        File dir = new File(directory);
        //폴더자체가 없다
        if (!dir.exists()) {
            return null;
        }
        return (List<File>) FileUtils.listFiles(dir, FileFilterUtils.prefixFileFilter(prefix), null);
    }
}

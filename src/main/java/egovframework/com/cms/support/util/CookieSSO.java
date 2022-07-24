package egovframework.com.cms.support.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

@Slf4j
public class CookieSSO {
    private static final int LIFE_LIMIT_SECONDS = 60 * 60 * 12;

    public static void setSSOCookie(HttpServletRequest request, HttpServletResponse response, String prefix, String userId, long currentTimeMillis, int life, String cookieDomain, String path) {
        int currentTimeSeconds = (int) currentTimeMillis / 1000;
        try {
            String ssoCookieKey = prefix + DigestUtils.sha256Hex(cookieDomain);
            int expiry;
            if (life == -1) {
                expiry = -1;
            } else {
                expiry = currentTimeSeconds + life;
            }

            String hash = DigestUtils.sha256Hex(userId + (currentTimeSeconds + "") + expiry + DigestUtils.sha256Hex(userId + (currentTimeSeconds + "") + expiry) + Utils.getRemoteIp(request));
            String ssoCookieValue = userId + "|" + (currentTimeSeconds + "") + "|" + expiry + "|" + hash;
            ssoCookieValue = URLEncoder.encode(ssoCookieValue, "UTF-8");
            setCookie(response, ssoCookieKey, ssoCookieValue, life, cookieDomain, path);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }

    public static void setSSOCookie(HttpServletRequest request, HttpServletResponse response, String prefix, String userId, long currentTimeMillis, int life, String cookieDomain) {
        setSSOCookie(request, response, prefix, userId, currentTimeMillis, life, cookieDomain, "/");
    }

    public static String getUserId(HttpServletRequest request, HttpServletResponse response, Cookie cookie, String cookieDomain, String path) {
        if (cookie == null) {
            log.info("CookieSSO cookie is null.");
            return null;
        }
        String cv = null;
        try {
            cv = URLDecoder.decode(cookie.getValue(), "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        assert cv != null;
        String[] temp = cv.split("\\|");
        if (temp.length != 4) {
            deleteCookie(response, cookie, cookieDomain, path);
            log.info("CookieSSO ##### SSO Cookie is modified. Array length does not matched.");
            return null;
        }
        String userId = temp[0];
        String created = temp[1];
        String expiry = temp[2];
        String hmac = temp[3];

        String hash;
        hash = DigestUtils.sha256Hex(userId + created + expiry + DigestUtils.sha256Hex(userId + created + expiry) + Utils.getRemoteIp(request));
        if (!hmac.equals(hash)) {
            deleteCookie(response, cookie, cookieDomain, path);
            log.info("CookieSSO ##### SSO Cookie is modified. Hash value does not matched.");
            return null;
        }
        int now = (int) (System.currentTimeMillis() / 1000);
        if ("-1".equals(expiry)) {
            if (now > Integer.parseInt(created) + LIFE_LIMIT_SECONDS) {
                deleteCookie(response, cookie, cookieDomain, path);
                log.info("CookieSSO ##### SSO Cookie is expired. Cookie is older than LIFE_LIMIT_MILLISECONDS[{}].", LIFE_LIMIT_SECONDS);
                return null;
            }
        }
        if (!"-1".equals(expiry) && Integer.parseInt(expiry) < now) {
            deleteCookie(response, cookie, cookieDomain, path);
            log.info("CookieSSO ##### SSO Cookie is expired. Expiry passed.");
            return null;
        }
        return userId;

    }

    public static String getUserId(HttpServletRequest request, HttpServletResponse response, String prefix, String cookieDomain) {
        String ssoCookieKey = prefix + DigestUtils.sha256Hex(cookieDomain);
        return getUserId(request, response, getCookie(request, ssoCookieKey), cookieDomain, "/");
    }

    public static Cookie getCookie(HttpServletRequest request, String key) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (key.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    public static Cookie getSSOCookie(HttpServletRequest request, String prefix, String cookieDomain) {
        String ssoCookieKey = prefix + DigestUtils.sha256Hex(cookieDomain);
        return getCookie(request, ssoCookieKey);
    }

    private static void setCookie(HttpServletResponse response, String key, String value, int expiry, String domain, String path) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(expiry);
        cookie.setDomain(domain);
        cookie.setPath(path);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private static void deleteCookie(HttpServletResponse response, Cookie cookie, String domain, String path) {
        if (cookie != null) {
            cookie.setDomain(domain);
            cookie.setPath(path);
            cookie.setMaxAge(0);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }
    }

    public static void deleteSSOCookie(HttpServletResponse response, String prefix, String cookieDomain, String path) {
        String ssoCookieKey = prefix + DigestUtils.sha256Hex(cookieDomain);
        Cookie cookie = new Cookie(ssoCookieKey, "");
        cookie.setDomain(cookieDomain);
        cookie.setPath(path);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}

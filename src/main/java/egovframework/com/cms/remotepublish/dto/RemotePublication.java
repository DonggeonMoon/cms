package egovframework.com.cms.remotepublish.dto;

import egovframework.com.cms.archive.dto.Archive;
import egovframework.com.cms.board.dto.BoardArticle;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.support.util.Utils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.safety.Whitelist;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@NoArgsConstructor
@Getter
@Setter
public class RemotePublication {
    public String title;
    public String content;
    public String link;

    public RemotePublication(Archive archive) {
        this.title = archive.getArcTitle();
        try {
            this.content = new String(Utils.getJsoupFilteredText(archive.getArcContentHtml(), Whitelist.none(), true, false).getBytes("UTF-8"), "8859_1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String fullHost = Utils.getSchemeDomainPort(request);

        Site currentSite = (Site) request.getAttribute("currentSite");
        this.link = fullHost + Utils.getAppPath(currentSite) + "/archive/" + archive.getArcId();
    }

    public RemotePublication(BoardArticle boardArticle) {
    }
}

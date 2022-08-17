package egovframework.com.cms.archive.dto;


import egovframework.com.cms.fileinfo.dto.FileInfo;
import egovframework.com.cms.member.model.Member;
import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.support.Constant;
import egovframework.com.cms.support.annotation.ArchiveItem;
import egovframework.com.cms.support.util.Utils;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@ArchiveItem(customType = "archive", label = "아카이브")
@Getter
@Setter
public class Archive extends MultiSiteVO {
    @WatchDog
    private Integer arcId;
    private String arcCustomType;
    private Member member;
    @WatchDog
    private String arcTitle;
    private String arcSlug;
    private String arcContentHtml;
    private String arcPlainText;
    private String arcExcerpt;
    private List<ArchiveCategory> arcCategories;
    private List<ArchiveTag> arcTags;
    private String arcTemplate;
    private Date arcCreated;
    private Date arcLastUpdated;
    private String arcStatus = "publish";
    private Integer arcHit;
    private Integer arcRecommend;
    private boolean arcSupportComment = false;//댓글 기능 사용여부
    private Integer arcCommentTotal;

    private boolean archiveDataGenerated = false;

    private FileInfo thumb;

    private String[] arcRemotePublish;//원격출판대상
    private String arcCreatedFix;//등록일 수정필드..

    private String arcMetaCode1;
    private String arcMetaCode2;
    private String arcMetaCode3;

    private String arcSubText;//부가텍스트, 자막등

    private List<FileInfo> arcMedias;
    private String arcNuri = "";//공공누리코드

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((arcId == null) ? 0 : arcId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Archive other = (Archive) obj;
        if (arcId == null) {
            if (other.arcId != null)
                return false;
        } else if (!arcId.equals(other.arcId))
            return false;
        return true;
    }

    public void prepareArchiveData(String formMode) {
        //제목
        this.setArcTitle(StringEscapeUtils.unescapeHtml4(Jsoup.clean(this.getArcTitle(), Whitelist.none())));
        //등록일시 변경
        if (StringUtils.isNotBlank(this.getArcCreatedFix())) {
            try {
                this.setArcCreated(Constant.YYYYMMDDHHMMSS.parse(this.getArcCreatedFix()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            if ("INSERT".equals(formMode)) {
                //생성일
                this.setArcCreated(new Date());
            } else {
                //nothing...
            }
        }
        //마지막수정일
        if (this.getArcLastUpdated() == null) {
            this.setArcLastUpdated(new Date());
        }
        //슬러그
        if (StringUtils.isNotBlank(this.getArcTitle()) && StringUtils.isBlank(this.getArcSlug())) {
            this.setArcSlug(Utils.string2Slug(this.getArcTitle()));
        } else if (StringUtils.isNotBlank(this.getArcTitle()) && StringUtils.isNotBlank(this.getArcSlug())) {
            this.setArcSlug(Utils.string2Slug(this.getArcSlug()));
        }
        //텍스트
        this.setArcPlainText(StringEscapeUtils.unescapeHtml4(Jsoup.clean(this.getArcContentHtml(), Whitelist.none())));
        //발췌(요약)
        if (StringUtils.isBlank(this.getArcExcerpt())) {
            this.setArcExcerpt(StringUtils.abbreviate(this.getArcPlainText(), 200));
        }
        if (this.arcHit == null) {
            this.arcHit = 0;
        }
        if (this.arcRecommend == null) {
            this.arcRecommend = 0;
        }
        archiveDataGenerated = true;
    }

    public void makeTagLink(final List<ArchiveTag> tagList, Site currentSite) {
        StringBuilder sb = new StringBuilder(300);
        for (ArchiveTag archiveTag : tagList) {
            if (archiveTag.getTagName().length() < 3) {
                continue;
            }
            sb = new StringBuilder(500);
            sb.append("<a href=\"");
            sb.append(Utils.getAppPath(currentSite));
            sb.append("/archive/tag/");
            sb.append(archiveTag.getTagSlug());
            sb.append("\">");
            sb.append(archiveTag.getTagName());
            sb.append("</a>");
            this.setArcContentHtml(StringUtils.replace(this.getArcContentHtml(), archiveTag.getTagName(), sb.toString()));
        }
    }

    public List<ArchiveMediaRelation> extractArchiveMediaRelations() {
        if (StringUtils.isBlank(this.getArcContentHtml())) {
            //return null;
        }
        if (this.getArcId() == null || this.getArcId() == 0) {
            return null;
        }
        Pattern p = Pattern.compile("id=\"media_(\\d+)\"");
        Matcher m = p.matcher(this.getArcContentHtml());
        ArchiveMediaRelation archiveMediaRelation = null;
        List<ArchiveMediaRelation> list = new ArrayList<ArchiveMediaRelation>();
        List<Integer> duplicateCheckList = new ArrayList<Integer>();
        while (m.find()) {
            Integer fileId = Integer.parseInt(m.group(1));
            if (!duplicateCheckList.contains(fileId)) {
                archiveMediaRelation = new ArchiveMediaRelation();
                archiveMediaRelation.setArcId(this.getArcId());
                archiveMediaRelation.setFileId(fileId);
                list.add(archiveMediaRelation);
                duplicateCheckList.add(fileId);
            }
        }
        //파일 첨부로 넘어온거
        if (this.getArcMedias() != null && this.getArcMedias().size() > 0) {
            for (FileInfo fi : this.getArcMedias()) {
                Integer fileId = fi.getFileId();
                if (!duplicateCheckList.contains(fileId)) {
                    archiveMediaRelation = new ArchiveMediaRelation();
                    archiveMediaRelation.setArcId(this.getArcId());
                    archiveMediaRelation.setFileId(fileId);
                    list.add(archiveMediaRelation);
                    duplicateCheckList.add(fileId);
                }
            }
        }
        return list;
    }

    public String getPermLink() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Site currentSite = (Site) request.getAttribute("currentSite");
        if ("archive".equals(this.getArcCustomType())) {
            return Utils.getAppPath(currentSite) + "/archive/" + this.getArcId();
        } else {
            return Utils.getAppPath(currentSite) + "/archive/" + this.getArcCustomType() + "/" + this.getArcSlug();
        }
    }

    public ArchiveCategory getFirstArcCategory() {
        if (this.getArcCategories() != null && !this.getArcCategories().isEmpty()) {
            if (this.getArcCategories().get(0) != null && this.getArcCategories().get(0).getCatId() > 0) {
                return this.getArcCategories().get(0);
            }
        }
        return null;
    }
}
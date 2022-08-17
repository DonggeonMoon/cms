package egovframework.com.cms.archive.dto;

import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.support.util.Utils;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Getter
@Setter
public class ArchiveCategory extends ArchiveTaxonomy {
    @WatchDog
    private Integer catId;
    @WatchDog
    private String catName;
    private String catSlug;
    private List<Archive> catArchives;
    private Integer catArchiveCount;//컬럼없음
    private String catTemplate;
    private String catViewTemplate;
    private String catAdminListSkin;
    private String catAdminFormSkin;
    private String catCustomType = "post";
    private Integer catPageSize = 10;

    //메타 코드 필드 추가
    private String catMetaCode1;
    private String catMetaCode2;
    private String catMetaCode3;

    //기본정렬 지정
    private String catSortOrder;
    private String catSortDirection = "DESC";

    //작성자이름 강제표시
    private String catForceName;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((catId == null) ? 0 : catId.hashCode());
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
        ArchiveCategory other = (ArchiveCategory) obj;
        if (catId == null) {
            if (other.catId != null)
                return false;
        } else if (!catId.equals(other.catId))
            return false;
        return true;
    }

    public String getPermLink() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Site currentSite = (Site) request.getAttribute("currentSite");
        if ("archive".equals(this.getCatCustomType())) {
            return Utils.getAppPath(currentSite) + "/archive/category/" + this.getCatSlug();
        } else {
            return Utils.getAppPath(currentSite) + "/archive/" + this.getCatCustomType() + "/category/" + this.getCatSlug();
        }
    }

    @Override
    public void generateTaxonomyValues() {
        //슬러그 입력이 없으면 제목을 변환
        if (StringUtils.isBlank(this.getCatSlug())) {
            this.setCatSlug(Utils.string2Slug(this.getCatName()));
        }
        //슬러그 입력되었을 때는 입력된걸 변환
        else {
            this.setCatSlug(Utils.string2Slug(this.getCatSlug()));
        }
    }
}

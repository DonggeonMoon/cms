package egovframework.com.cms.archive.dto;

import egovframework.com.cms.support.util.Utils;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Getter
@Setter
public class ArchiveTag extends ArchiveTaxonomy {
    @WatchDog
    private Integer tagId;
    @WatchDog
    private String tagName;
    private String tagSlug;
    private List<Archive> tagArchives;
    private Integer tagArchiveCount;//아카이브개수

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tagId == null) ? 0 : tagId.hashCode());
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
        ArchiveTag other = (ArchiveTag) obj;
        if (tagId == null) {
            if (other.tagId != null)
                return false;
        } else if (!tagId.equals(other.tagId))
            return false;
        return true;
    }

    @Override
    public void generateTaxonomyValues() {
        this.setTagName(Utils.string2TagName(this.getTagName()));
        //슬러그 입력이 없으면 제목을 변환
        if (StringUtils.isBlank(this.getTagSlug())) {
            this.setTagSlug(Utils.string2Slug(this.getTagName()));
        }
        //슬러그 입력되었을 때는 입력된걸 변환
        else {
            this.setTagSlug(Utils.string2Slug(this.getTagSlug()));
        }
    }
}
package egovframework.com.cms.archive.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Getter
@Setter
public class ArchiveSearch extends PagingSearch {
    private Integer arcId;
    private String arcCustomType;
    private String arcStatus;
    private String arcSlug;
    private Integer catId;
    private String catSlug;
    private Integer tagId;
    private String tagSlug;
    private String memberId;

    //Meta Code
    private String metaCode1Cat;
    private String metaCode2Cat;
    private String metaCode3Cat;
    private String metaCode1;
    private String metaCode2;
    private String metaCode3;

    private String arcViewMode;

    //include exclude 추가 - catId 랑 같이 쓰면 안됨
    private List<Integer> arcInCats;//특정카테고리만 복수 지정할 경우 사용
    private List<Integer> arcOutCats;//특정 카테고리만 복주 제외할 경우 사용

    private List<Integer> arcIdNotIn;

    @Override
    public String getQueryString() {

        StringBuilder sb = new StringBuilder(100);
        sb.append(super.getQueryString());

        if (this.getTagId() != null && this.getTagId() > 0) {
            sb.append("&amp;tagId=");
            sb.append(this.getTagId());
        }
        if (this.getCatId() != null && this.getCatId() > 0) {
            sb.append("&amp;catId=");
            sb.append(this.getCatId());
        }
        if (StringUtils.isNotBlank(this.getMetaCode1Cat())) {
            sb.append("&amp;metaCode1Cat=");
            sb.append(this.getMetaCode1Cat());
        }
        if (StringUtils.isNotBlank(this.getMetaCode1())) {
            sb.append("&amp;metaCode1=");
            sb.append(this.getMetaCode1());
        }
        if (StringUtils.isNotBlank(this.getMetaCode2Cat())) {
            sb.append("&amp;metaCode2Cat=");
            sb.append(this.getMetaCode2Cat());
        }
        if (StringUtils.isNotBlank(this.getMetaCode2())) {
            sb.append("&amp;metaCode2=");
            sb.append(this.getMetaCode2());
        }
        if (StringUtils.isNotBlank(this.getMetaCode3Cat())) {
            sb.append("&amp;metaCode3Cat=");
            sb.append(this.getMetaCode3Cat());
        }
        if (StringUtils.isNotBlank(this.getMetaCode3())) {
            sb.append("&amp;metaCode3=");
            sb.append(this.getMetaCode3());
        }
        if (StringUtils.isNotBlank(this.getArcViewMode())) {
            sb.append("&amp;arcViewMode=");
            sb.append(this.getArcViewMode());
        }
        return sb.toString();
    }
}

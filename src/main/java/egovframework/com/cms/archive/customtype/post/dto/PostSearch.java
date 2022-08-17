package egovframework.com.cms.archive.customtype.post.dto;

import egovframework.com.cms.archive.dto.ArchiveSearch;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class PostSearch extends ArchiveSearch {
    private String poSticky;
    private String poSelected1;
    private String poSelected2;

    @Override
    public String getQueryString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(super.getQueryString());

        if (StringUtils.isNotBlank(this.getPoSelected1())) {
            sb.append("&amp;poSelected1=");
            sb.append(this.getPoSelected1());
        }
        if (StringUtils.isNotBlank(this.getPoSelected2())) {
            sb.append("&amp;poSelected2=");
            sb.append(this.getPoSelected2());
        }

        return sb.toString();
    }
}

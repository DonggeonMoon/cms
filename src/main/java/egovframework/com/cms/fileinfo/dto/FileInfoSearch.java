package egovframework.com.cms.fileinfo.dto;

import egovframework.com.cms.support.pagination.PagingSearch;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class FileInfoSearch extends PagingSearch {
    private String fileModule;
    private String fileModuleId;
    private String fileModuleSub;
    private String fileModuleSubId;
    private String fileMediaType;

    @Override
    public String getQueryString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(super.getQueryString());

        if (StringUtils.isNotBlank(this.getFileMediaType())) {
            sb.append("&amp;fileMediaType=");
            sb.append(this.getFileMediaType());
        }
        if (StringUtils.isNotBlank(this.getFileModule())) {
            sb.append("&amp;fileModule=");
            sb.append(this.getFileModule());
        }
        return sb.toString();
    }
}

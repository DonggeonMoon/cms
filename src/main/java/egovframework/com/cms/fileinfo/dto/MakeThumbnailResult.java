package egovframework.com.cms.fileinfo.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class MakeThumbnailResult {
    private File resultFile;
    private Integer oriImageWidth;
    private Integer oriImageHeight;
}

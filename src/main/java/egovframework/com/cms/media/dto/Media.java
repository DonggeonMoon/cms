package egovframework.com.cms.media.dto;

import egovframework.com.cms.fileinfo.dto.FileInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class Media extends FileInfo {
    private MultipartFile mediaFile;
}

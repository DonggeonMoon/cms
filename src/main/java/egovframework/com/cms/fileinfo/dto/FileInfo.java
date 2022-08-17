package egovframework.com.cms.fileinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import egovframework.com.cms.archive.dto.Archive;
import egovframework.com.cms.site.dto.MultiSiteVO;
import egovframework.com.cms.support.util.Utils;
import egovframework.com.cms.watchdog.aop.WatchDog;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
public class FileInfo extends MultiSiteVO {
    @WatchDog
    private Integer fileId = 0;
    private String memberId;
    private String fileModule;
    private String fileModuleId;
    private String fileModuleSub;
    private String fileModuleSubId;
    private String fileOriginalName;
    private String filePath;
    private String fileThumbnailPath = "";
    private String fileExt;
    private String fileMimeType;
    private String fileMediaType;//IMAGE, DOCUMENT, VIDEO, AUDIO, ETC
    private long fileSize;
    private Date fileRegDate;
    private String fileAltText = "";
    private Integer fileDownloadCount;

    //컬럼없음 - 업로드 결과체크용
    private boolean fileUploadSuccess = true;
    private int fileUploadErrorCode;

    private String fileSizeString;//1KB, 1MB...
    //첨부된 곳 아카이브만...
    @JsonIgnore
    private Set<Archive> fileAttachedArchiveSet;

    //151207 추가
    private String fileUUID;//파일 고유아이디

    private Integer fileOriImageWidth = 0;    //이미지가로길이
    private Integer fileOriImageHeight = 0;    //이미지세로길이

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
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
        FileInfo other = (FileInfo) obj;
        if (fileId == null) {
            if (other.fileId != null)
                return false;
        } else if (!fileId.equals(other.fileId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FileInfo\n[fileId=" + fileId + "\n, memberId=" + memberId + "\n, fileModule=" + fileModule + "\n, fileModuleId=" + fileModuleId + "\n, fileModuleSub=" + fileModuleSub
                + "\n, fileModuleSubId=" + fileModuleSubId + "\n, fileOriginalName=" + fileOriginalName + "\n, filePath=" + filePath + "\n, fileThumbnailPath=" + fileThumbnailPath + "\n, fileExt="
                + fileExt + "\n, fileMimeType=" + fileMimeType + "\n, fileMediaType=" + fileMediaType + "\n, fileSize=" + fileSize + "\n, fileRegDate=" + fileRegDate + "\n, fileAltText="
                + fileAltText + "\n, fileDownloadCount=" + fileDownloadCount + "\n, fileUploadSuccess=" + fileUploadSuccess + "\n, fileUploadErrorCode=" + fileUploadErrorCode + "\n, fileSizeString="
                + fileSizeString + "\n, fileAttachedArchiveSet=" + fileAttachedArchiveSet + "]\n";
    }

    public String getFileModuleId() {
        return fileModuleId == null ? "" : fileModuleId;
    }

    public String getFileModuleSub() {
        return fileModuleSub == null ? "" : fileModuleSub;
    }

    public String getFileModuleSubId() {
        return fileModuleSubId == null ? "" : fileModuleSubId;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
        this.fileSizeString = Utils.getFileSizeString(fileSize, true, true);
    }
}
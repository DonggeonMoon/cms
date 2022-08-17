package egovframework.com.cms.fileinfo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FileInfoUploadResult {
    public static final int ERROR_NOT_UPLOADABLE_FILE = 100;
    public static final int ERROR_UPLOAD_SIZE_OVER_FILE = 200;
    public static final int ERROR_ALT_TEXT_MISSING_FILE = 300;
    public static final int ERROR_USE_MULTIMEDIA_UPLOADER = 400;
    public static final int ERROR_USE_NORMAL_UPLOADER = 500;
    public static final int ERROR_NO_FILE = 999;

    private List<String> notUploadableFiles = new ArrayList<String>();
    private List<String> uploadSizeOverFiles = new ArrayList<String>();
    private List<String> altTextMissingFiles = new ArrayList<String>();

    public void addErrorInfo(FileInfo fileInfo) {
        if (!fileInfo.isFileUploadSuccess()) {
            if (fileInfo.getFileUploadErrorCode() == ERROR_NOT_UPLOADABLE_FILE) {
                this.notUploadableFiles.add(fileInfo.getFileOriginalName());
            }
            if (fileInfo.getFileUploadErrorCode() == ERROR_UPLOAD_SIZE_OVER_FILE) {
                this.uploadSizeOverFiles.add(fileInfo.getFileOriginalName());
            }
            if (fileInfo.getFileUploadErrorCode() == ERROR_ALT_TEXT_MISSING_FILE) {
                this.altTextMissingFiles.add(fileInfo.getFileOriginalName());
            }
        }
    }

    public boolean hasErrors() {
        if (!this.notUploadableFiles.isEmpty() || !this.uploadSizeOverFiles.isEmpty() || !this.altTextMissingFiles.isEmpty()) {
            return true;
        }
        return false;
    }
}

package egovframework.com.cms.watchdog.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class WatchDogLogFile implements Comparable<WatchDogLogFile> {
    private String fileYear;
    private String fileMonth;
    private String fileName;
    private long fileSize;
    private Date fileRegDate;

    @Override
    public int compareTo(WatchDogLogFile file) {
        if (file.getFileRegDate().after(this.getFileRegDate())) {
            return 1;
        } else {
            return -1;
        }
    }
}

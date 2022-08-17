package egovframework.com.cms.watchdog.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class WatchDogLog {
    private int logId;
    private String memberId;
    private String methodName;
    private Date logDate;
    private String logUri;
    private String logReport;
    private String logRemoteIp;
}

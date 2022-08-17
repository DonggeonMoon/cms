package egovframework.com.cms.mail.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MailTrack {
    private Integer mtIdx;
    private String mtEmail;
    private String mtSubject;
    private Date mtSendDate;
    private boolean mtOpened;
}

package egovframework.com.cms.mail.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MailTrack {
    private Integer mailTrackIndex;
    private String mailTrackEmail;
    private String mailTrackSubject;
    private LocalDate mailTrackSendDate;
    private boolean isMailTrackOpened;
}

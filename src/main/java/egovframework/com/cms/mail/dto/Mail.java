package egovframework.com.cms.mail.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Mail {
    private String mailToEmail;
    private String mailFromEmail;
    private String mailFromName;
    private String mailSubject;
    private String mailContent;

    public String[] getToEmails() {
        return this.getMailToEmail().split(",");
    }
}

package egovframework.com.cms.mail.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Subscriber {
    private Integer subscriberIndex;
    private String subscriberEmail;
    private String subscriberService;
    private LocalDate subscriberRegistrationDate;
}

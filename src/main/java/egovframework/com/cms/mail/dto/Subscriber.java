package egovframework.com.cms.mail.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Subscriber {
    private Integer subscrIdx;
    private String subscrEmail;
    private String subscrService;
    private Date subscrRegDate;
}

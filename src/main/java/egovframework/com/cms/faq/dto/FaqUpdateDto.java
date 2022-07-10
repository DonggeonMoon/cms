package egovframework.com.cms.faq.dto;

import lombok.Data;

@Data
public class FaqUpdateDto {

    private Long id;

    private String question;
    private String answer;

    private Boolean publish;
}

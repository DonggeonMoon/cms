package egovframework.com.cms.faq.web;

import egovframework.com.cms.faq.dto.FaqCreateDto;
import egovframework.com.cms.faq.dto.FaqUpdateDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class FaqValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof FaqCreateDto) {
            FaqCreateDto dto = (FaqCreateDto) target;
            validate(dto.getQuestion(), dto.getAnswer(), dto.getPublish(), errors);
            return;
        }

        if (target instanceof FaqUpdateDto) {
            FaqUpdateDto dto = (FaqUpdateDto) target;
            validate(dto.getQuestion(), dto.getAnswer(), dto.getPublish(), errors);
        }
    }

    private void validate(String question, String answer, Boolean publish, Errors errors) {
        if (!StringUtils.hasText(question)) errors.rejectValue("question", null, "질문을 작성해주십시오.");
        if (!StringUtils.hasText(answer)) errors.rejectValue("answer", null, "답변을 작성해주십시오.");
        if (publish == null) errors.rejectValue("publish", null, "게시 상태를 설정해주십시오.");
    }
}

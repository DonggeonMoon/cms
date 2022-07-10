package egovframework.com.cms.faq.service;

import egovframework.com.cms.faq.dto.FaqCreateDto;
import egovframework.com.cms.faq.dto.FaqSearch;
import egovframework.com.cms.faq.dto.FaqUpdateDto;
import egovframework.com.cms.faq.model.Faq;
import egovframework.com.cms.support.Pagination.Paging;

public interface FaqService {
    void saveFaq(FaqCreateDto faqCreateDto);

    Faq findFaqById(Long id) throws Exception;

    Paging findPage(FaqSearch faqSearch);

    void updateFaq(FaqUpdateDto faqUpdateDto) throws Exception;

    void deleteFaq(Long id) throws Exception;
}

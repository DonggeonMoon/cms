package egovframework.com.cms.faq.service.impl;

import egovframework.com.cms.faq.dto.FaqCreateDto;
import egovframework.com.cms.faq.dto.FaqResponseDto;
import egovframework.com.cms.faq.dto.FaqSearch;
import egovframework.com.cms.faq.dto.FaqUpdateDto;
import egovframework.com.cms.faq.model.Faq;
import egovframework.com.cms.faq.repository.FaqRepository;
import egovframework.com.cms.faq.service.FaqService;
import egovframework.com.cms.support.pagination.Paging;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FaqServiceImpl implements FaqService {

    private final FaqRepository faqRepository;

    @Override
    public void saveFaq(FaqCreateDto faqCreateDto) {
        Faq faq = Faq.of(
                faqCreateDto.getQuestion(),
                faqCreateDto.getAnswer(),
                faqCreateDto.getPublish());

        faqRepository.save(faq);
    }

    @Override
    @Transactional(readOnly = true)
    public Faq findFaqById(Long id) throws Exception {
        return faqRepository.findById(id)
                .orElseThrow(() -> new Exception("FAQ가 존재하지 않습니다."));
    }

    @Override
    @Transactional(readOnly = true)
    public Paging findPage(FaqSearch faqSearch) {
        List<FaqResponseDto> list = faqRepository.findList(faqSearch).stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        int rowTotal = faqRepository.count(faqSearch);

        return new Paging(list, rowTotal, faqSearch);
    }

    @Override
    public void updateFaq(FaqUpdateDto faqUpdateDto) throws Exception {
        Faq faq = faqRepository.findById(faqUpdateDto.getId())
                .orElseThrow(() -> new Exception("FAQ가 존재하지 않습니다."));

        faqRepository.updateFaq(faq, faqUpdateDto);
    }

    @Override
    public void deleteFaq(Long id) throws Exception {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new Exception("FAQ가 존재하지 않습니다."));

        faqRepository.delete(faq);
    }

    // == 편의 메소드
    private FaqResponseDto toDto(Faq faq) {
        FaqResponseDto dto = new FaqResponseDto();

        dto.setId(faq.getId());
        dto.setQuestion(divideParagraph(faq.getQuestion()));
        dto.setAnswer(divideParagraph(faq.getAnswer()));
        dto.setPublish(faq.getPublish());

        return dto;
    }

    private String divideParagraph(String target) {

        // 문단 구분은 \r\n\r\n로 진행됨
        // String을 검사하여 \r\n이 있으면 <br>의 html로 바꾸는 작업
        return target.replaceAll("\r\n", "<br>");
    }
}

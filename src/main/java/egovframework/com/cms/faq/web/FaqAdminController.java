package egovframework.com.cms.faq.web;

import egovframework.com.cms.faq.dto.FaqCreateDto;
import egovframework.com.cms.faq.dto.FaqResponseDto;
import egovframework.com.cms.faq.dto.FaqSearch;
import egovframework.com.cms.faq.dto.FaqUpdateDto;
import egovframework.com.cms.faq.model.Faq;
import egovframework.com.cms.faq.service.FaqService;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.support.Constant;
import egovframework.com.cms.support.Pagination.Paging;
import egovframework.com.cms.support.ServerMessage;
import egovframework.com.cms.support.annotation.CurrentSite;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class FaqAdminController {
    private final FaqService faqService;
    private final FaqValidator faqValidator;

    @GetMapping(Constant.APP_PATH + Constant.SITE_ID_PATH + Constant.ADMIN_PATH + "/faq/list.do")
    public String listGet(@CurrentSite Site currentSite, @ModelAttribute FaqSearch faqSearch, Model model) throws Exception {
        faqSearch.setPaging(true);
        faqSearch.setSitePrefix(currentSite.getSitePrefix());
        faqSearch.fixBrokenSvByDefaultCharsets();

        Paging paging = faqService.findPage(faqSearch);

        model.addAttribute("paging", paging);

        return "admin/faq/list";
    }

    @GetMapping(Constant.APP_PATH + Constant.SITE_ID_PATH + Constant.ADMIN_PATH + "/faq/insert.do")
    public String insertPageGet(@CurrentSite Site currentSite, Model model) {
        model.addAttribute("formMode", "INSERT");
        return "admin/faq/form";
    }

    @PostMapping(Constant.APP_PATH + Constant.SITE_ID_PATH + Constant.ADMIN_PATH + "/faq/insert.do")
    public String insertPost(@ModelAttribute FaqCreateDto faqCreateDto, BindingResult bindingResult, @CurrentSite Site currentSite, Model model) {
        faqValidator.validate(faqCreateDto, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "INSERT");
            model.addAttribute("origin", toDto(faqCreateDto));
            return "admin/faq/form";
        }

        faqService.saveFaq(faqCreateDto);

        return "redirect:list.do";
    }

    @GetMapping(Constant.APP_PATH + Constant.SITE_ID_PATH + Constant.ADMIN_PATH + "/faq/update.do/{id}")
    public String updatePageGet(@CurrentSite Site currentSite, @PathVariable Long id, Model model) throws Exception {
        Faq faq = faqService.findFaqById(id);

        model.addAttribute("formMode", "UPDATE");
        model.addAttribute("origin", toDto(faq));

        return "admin/faq/form";
    }

    @PostMapping(Constant.APP_PATH + Constant.SITE_ID_PATH + Constant.ADMIN_PATH + "/faq/update.do")
    public String updatePost(@CurrentSite Site currentSite, @ModelAttribute FaqUpdateDto faqUpdateDto, BindingResult bindingResult, Model model) throws Exception {
        faqValidator.validate(faqUpdateDto, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "UPDATE");
            model.addAttribute("origin", toDto(faqUpdateDto));
            return "admin/faq/form";
        }

        faqService.updateFaq(faqUpdateDto);

        return "redirect:list.do";
    }

    @PostMapping(Constant.APP_PATH + Constant.SITE_ID_PATH + Constant.ADMIN_PATH + "/faq/clear.do")
    @ResponseBody
    public ServerMessage deletePost(@CurrentSite Site currentSite, @RequestParam Long id) throws Exception {
        ServerMessage serverMessage = new ServerMessage();

        faqService.deleteFaq(id); // 삭제에 실패하면 service단에서 예외가 터짐

        // 아래의 로직은 성공 로직. (실패는 위에서 예외가 터지므로 이곳으로 도달 안됨)
        serverMessage.setSuccess(true);
        serverMessage.setText("삭제 완료하였습니다.");

        return serverMessage;
    }

    private FaqResponseDto toDto(Faq faq) {
        FaqResponseDto dto = new FaqResponseDto();

        dto.setId(faq.getId());
        dto.setQuestion(faq.getQuestion());
        dto.setAnswer(faq.getAnswer());
        dto.setPublish(faq.getPublish());

        return dto;
    }

    private FaqResponseDto toDto(FaqCreateDto faqCreateDto) {
        FaqResponseDto dto = new FaqResponseDto();

        dto.setId(faqCreateDto.getId());
        dto.setQuestion(faqCreateDto.getQuestion());
        dto.setAnswer(faqCreateDto.getAnswer());
        dto.setPublish(faqCreateDto.getPublish());

        return dto;
    }

    private FaqResponseDto toDto(FaqUpdateDto faqUpdateDto) {
        FaqResponseDto dto = new FaqResponseDto();

        dto.setId(faqUpdateDto.getId());
        dto.setQuestion(faqUpdateDto.getQuestion());
        dto.setAnswer(faqUpdateDto.getAnswer());
        dto.setPublish(faqUpdateDto.getPublish());

        return dto;
    }
}

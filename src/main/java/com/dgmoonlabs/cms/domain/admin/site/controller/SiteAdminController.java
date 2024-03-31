package com.dgmoonlabs.cms.domain.admin.site.controller;

import com.dgmoonlabs.cms.domain.admin.site.dto.SiteRequest;
import com.dgmoonlabs.cms.domain.admin.site.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/site")
public class SiteAdminController {
    private final SiteService siteService;

    @GetMapping
    public String getSites(SiteRequest request, Pageable pageable, Model model) {
        model.addAttribute("site", siteService.getSite(request, pageable));
        return "/admin/site/list";
    }

    @GetMapping("/{id}")
    public String getSites(@PathVariable Long id, Model model) {
        model.addAttribute("site", siteService.getSiteById(id));
        return "/admin/site/view";
    }


    @PostMapping
    public String saveSite(@ModelAttribute SiteRequest request) {
        siteService.saveSite(request);
        return "redirect:/admin/site/list";
    }

    @GetMapping("/{id}/update")
    public String updateSite(@PathVariable Long id, Model model) {
        model.addAttribute("site", siteService.getSiteById(id));
        return "/admin/site/form";
    }

    @PutMapping
    public String updateSite(@ModelAttribute SiteRequest request) {
        siteService.updateSite(request);
        return "redirect:/admin/site/list";
    }
}

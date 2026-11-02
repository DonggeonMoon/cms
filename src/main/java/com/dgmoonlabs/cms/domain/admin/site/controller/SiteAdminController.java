package com.dgmoonlabs.cms.domain.admin.site.controller;

import com.dgmoonlabs.cms.domain.admin.site.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/site")
public class SiteAdminController {
    private final SiteService siteService;

    @GetMapping
    public String getSites(@RequestParam(required = false) String keyword, Pageable pageable, Model model) {
        model.addAttribute("site", siteService.searchSites(keyword, pageable));
        return "/admin/site/list";
    }

    @GetMapping("/{id}")
    public String getSite(@PathVariable Long id, Model model) {
        model.addAttribute("site", siteService.getSite(id));
        return "/admin/site/view";
    }

    @GetMapping("/{id}/update")
    public String updateSite(@PathVariable Long id, Model model) {
        model.addAttribute("site", siteService.getSite(id));
        return "/admin/site/form";
    }
}

package com.dgmoonlabs.cms.domain.admin.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashBoardController {
    @GetMapping("/admin/")
    public String showDashboard() {
        return "/admin/dashboard";
    }
}

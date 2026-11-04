package com.dgmoonlabs.cms.domain.admin.account.controller;

import com.dgmoonlabs.cms.domain.admin.account.dto.AdminAccountDtos.MyPermissions;
import com.dgmoonlabs.cms.domain.admin.account.service.AdminAccountService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminPermissionController {
    private final AdminAccountService adminAccountService;

    @GetMapping("/api/admin/me/permissions")
    public ResponseEntity<MyPermissions> myPermissions(@AuthenticationPrincipal final LoginUser admin) {
        return ResponseEntity.ok(adminAccountService.permissions(admin.id()));
    }
}

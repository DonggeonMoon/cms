package com.dgmoonlabs.cms.domain.admin.account.controller;

import com.dgmoonlabs.cms.domain.admin.account.dto.AdminAccountDtos.*;
import com.dgmoonlabs.cms.domain.admin.account.service.AdminAccountService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import com.dgmoonlabs.cms.global.security.RequiresReauthentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiresReauthentication
@RequiredArgsConstructor
public class AdminAccountController {
    private final AdminAccountService adminAccountService;

    @GetMapping("/api/admin/admin-grades")
    public ResponseEntity<List<GradeResponse>> grades() {
        return ResponseEntity.ok(adminAccountService.grades());
    }

    @GetMapping("/api/admin/admin-grades/areas")
    public ResponseEntity<List<AreaOption>> areas() {
        return ResponseEntity.ok(adminAccountService.areaOptions());
    }

    @PostMapping("/api/admin/admin-grades")
    public ResponseEntity<Void> createGrade(@RequestBody @Valid final GradeSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/admin/admin-grades/" + adminAccountService.createGrade(request))).build();
    }

    @PutMapping("/api/admin/admin-grades/{id}")
    public ResponseEntity<Void> updateGrade(@PathVariable final Long id, @RequestBody @Valid final GradeSaveRequest request) {
        adminAccountService.updateGrade(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/admin-grades/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable final Long id) {
        adminAccountService.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/admins")
    public ResponseEntity<List<AdminResponse>> admins() {
        return ResponseEntity.ok(adminAccountService.admins());
    }

    @PostMapping("/api/admin/admins")
    public ResponseEntity<Void> createAdmin(@RequestBody @Valid final AdminCreateRequest request) {
        return ResponseEntity.created(URI.create("/api/admin/admins/" + adminAccountService.createAdmin(request))).build();
    }

    @PutMapping("/api/admin/admins/{id}/grade")
    public ResponseEntity<Void> assignGrade(@PathVariable final Long id, @RequestBody final GradeAssignRequest request) {
        adminAccountService.assignGrade(id, request.gradeId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/admins/{id}/unlock")
    public ResponseEntity<Void> unlock(@PathVariable final Long id) {
        adminAccountService.unlock(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/admins/{id}")
    public ResponseEntity<Void> deleteAdmin(@AuthenticationPrincipal final LoginUser admin, @PathVariable final Long id) {
        adminAccountService.deleteAdmin(admin.id(), id);
        return ResponseEntity.noContent().build();
    }
}

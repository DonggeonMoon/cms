package com.dgmoonlabs.cms.domain.common.guestposting;

import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GuestPostingController {
    private final GuestPostingService service;

    @GetMapping("/api/questions/guest-posting")
    public ResponseEntity<GuestPostingPolicy> questions() {
        return ResponseEntity.ok(service.policy(SiteFeature.QNA));
    }

    @PutMapping("/api/admin/questions/guest-posting")
    public ResponseEntity<GuestPostingPolicy> changeQuestions(@RequestBody @Valid final GuestPostingPolicy request) {
        return ResponseEntity.ok(service.change(SiteFeature.QNA, request));
    }

    @GetMapping("/api/reports/guest-posting")
    public ResponseEntity<GuestPostingPolicy> reports() {
        return ResponseEntity.ok(service.policy(SiteFeature.REPORT));
    }

    @PutMapping("/api/admin/reports/guest-posting")
    public ResponseEntity<GuestPostingPolicy> changeReports(@RequestBody @Valid final GuestPostingPolicy request) {
        return ResponseEntity.ok(service.change(SiteFeature.REPORT, request));
    }
}

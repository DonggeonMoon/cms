package com.dgmoonlabs.cms.domain.business.record.controller;

import com.dgmoonlabs.cms.domain.business.record.dto.CustomerRecordDtos.CustomerRecordResponse;
import com.dgmoonlabs.cms.domain.business.record.dto.CustomerRecordDtos.CustomerRecordSaveRequest;
import com.dgmoonlabs.cms.domain.business.record.dto.CustomerRecordDtos.MyCustomerRecord;
import com.dgmoonlabs.cms.domain.business.record.service.CustomerRecordService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CustomerRecordController {
    private final CustomerRecordService customerRecordService;

    @GetMapping("/api/admin/customer-records")
    public ResponseEntity<PagedModel<CustomerRecordResponse>> search(@RequestParam(required = false) final Long memberId,
                                                                     @RequestParam(required = false) final Long reservationId,
                                                                     @RequestParam(required = false) final String keyword,
                                                                     @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(customerRecordService.search(memberId, reservationId, keyword, pageable)));
    }

    @PostMapping("/api/admin/customer-records")
    public ResponseEntity<CustomerRecordResponse> create(@AuthenticationPrincipal final LoginUser admin,
                                                         @RequestBody @Valid final CustomerRecordSaveRequest request) {
        return ResponseEntity.ok(customerRecordService.create(admin, request));
    }

    @PutMapping("/api/admin/customer-records/{id}")
    public ResponseEntity<CustomerRecordResponse> update(@PathVariable final Long id,
                                                         @RequestBody @Valid final CustomerRecordSaveRequest request) {
        return ResponseEntity.ok(customerRecordService.update(id, request));
    }

    @DeleteMapping("/api/admin/customer-records/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        customerRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/customer-records/me")
    public ResponseEntity<PagedModel<MyCustomerRecord>> mine(@AuthenticationPrincipal final LoginUser user,
                                                             @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(customerRecordService.getMine(user.id(), pageable)));
    }
}

package com.dgmoonlabs.cms.domain.business.status.controller;

import com.dgmoonlabs.cms.domain.business.status.constant.StatusDomain;
import com.dgmoonlabs.cms.domain.business.status.dto.StatusDtos.CustomStatusRequest;
import com.dgmoonlabs.cms.domain.business.status.dto.StatusDtos.StatusResponse;
import com.dgmoonlabs.cms.domain.business.status.dto.StatusDtos.StatusSaveRequest;
import com.dgmoonlabs.cms.domain.business.status.service.StatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatusController {
    private final StatusService statusService;

    @GetMapping("/api/statuses/{domain}")
    public ResponseEntity<List<StatusResponse>> statuses(@PathVariable final StatusDomain domain) {
        return ResponseEntity.ok(statusService.getStatuses(domain));
    }

    @PostMapping("/api/admin/statuses/{domain}")
    public ResponseEntity<StatusResponse> create(@PathVariable final StatusDomain domain, @RequestBody @Valid final StatusSaveRequest request) {
        return ResponseEntity.ok(statusService.create(domain, request));
    }

    @PutMapping("/api/admin/statuses/items/{id}")
    public ResponseEntity<StatusResponse> update(@PathVariable final Long id, @RequestBody @Valid final StatusSaveRequest request) {
        return ResponseEntity.ok(statusService.update(id, request));
    }

    @DeleteMapping("/api/admin/statuses/items/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        statusService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/admin/orders/{id}/custom-status")
    public ResponseEntity<Void> applyToOrder(@PathVariable final Long id, @RequestBody final CustomStatusRequest request) {
        statusService.applyToOrder(id, request.code());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/admin/reservations/{id}/custom-status")
    public ResponseEntity<Void> applyToReservation(@PathVariable final Long id, @RequestBody final CustomStatusRequest request) {
        statusService.applyToReservation(id, request.code());
        return ResponseEntity.noContent().build();
    }
}

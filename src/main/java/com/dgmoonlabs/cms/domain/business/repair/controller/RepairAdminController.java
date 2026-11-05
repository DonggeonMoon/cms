package com.dgmoonlabs.cms.domain.business.repair.controller;

import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderResponse;
import com.dgmoonlabs.cms.domain.business.repair.dto.RepairDtos.QuoteOrderRequest;
import com.dgmoonlabs.cms.domain.business.repair.dto.RepairDtos.RepairResponse;
import com.dgmoonlabs.cms.domain.business.repair.service.RepairRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/repairs")
@RequiredArgsConstructor
public class RepairAdminController {
    private final RepairRequestService repairRequestService;

    @GetMapping
    public ResponseEntity<PagedModel<RepairResponse>> search(@RequestParam(required = false) final String keyword,
                                                              @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(repairRequestService.search(keyword, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepairResponse> get(@PathVariable final Long id) {
        return ResponseEntity.ok(repairRequestService.get(id));
    }

    @PostMapping("/{id}/quote-order")
    public ResponseEntity<OrderResponse> createQuoteOrder(@PathVariable final Long id, @RequestBody @Valid final QuoteOrderRequest request) {
        return ResponseEntity.ok(repairRequestService.createQuoteOrder(id, request));
    }
}

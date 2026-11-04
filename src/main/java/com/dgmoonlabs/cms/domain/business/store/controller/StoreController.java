package com.dgmoonlabs.cms.domain.business.store.controller;

import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderResponse;
import com.dgmoonlabs.cms.domain.business.order.service.OrderService;
import com.dgmoonlabs.cms.domain.business.store.dto.StoreDtos.StoreSettingRequest;
import com.dgmoonlabs.cms.domain.business.store.dto.StoreDtos.StoreSettingResponse;
import com.dgmoonlabs.cms.domain.business.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;
    private final OrderService orderService;

    @GetMapping("/api/store-setting")
    public ResponseEntity<StoreSettingResponse> setting() {
        return ResponseEntity.ok(storeService.getSetting());
    }

    @PutMapping("/api/admin/store-setting")
    public ResponseEntity<StoreSettingResponse> save(@RequestBody @Valid final StoreSettingRequest request) {
        return ResponseEntity.ok(storeService.saveSetting(request));
    }

    @GetMapping("/api/admin/store-orders")
    public ResponseEntity<List<OrderResponse>> queue() {
        return ResponseEntity.ok(orderService.storeQueue());
    }
}

package com.dgmoonlabs.cms.domain.business.order.controller;

import com.dgmoonlabs.cms.domain.business.order.dto.CartDtos.CartAddRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.CartDtos.CartQuantityRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.CartDtos.CartResponse;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.CartCheckoutRequest;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderResponse;
import com.dgmoonlabs.cms.domain.business.order.service.CartService;
import com.dgmoonlabs.cms.domain.business.order.service.OrderService;
import com.dgmoonlabs.cms.domain.business.shipping.dto.ShippingDtos.ShippingPolicyRequest;
import com.dgmoonlabs.cms.domain.business.shipping.dto.ShippingDtos.ShippingPolicyResponse;
import com.dgmoonlabs.cms.domain.business.shipping.service.ShippingService;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.ShipmentRequest;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final OrderService orderService;
    private final ShippingService shippingService;

    @GetMapping("/api/cart")
    public ResponseEntity<CartResponse> cart(@AuthenticationPrincipal final LoginUser user,
                                             @RequestParam(required = false) final String postalCode) {
        return ResponseEntity.ok(cartService.get(user.id(), postalCode));
    }

    @PostMapping("/api/cart")
    public ResponseEntity<CartResponse> add(@AuthenticationPrincipal final LoginUser user,
                                            @RequestBody @Valid final CartAddRequest request) {
        return ResponseEntity.ok(cartService.add(user.id(), request));
    }

    @PutMapping("/api/cart/{id}")
    public ResponseEntity<CartResponse> changeQuantity(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id,
                                                       @RequestBody @Valid final CartQuantityRequest request) {
        return ResponseEntity.ok(cartService.changeQuantity(user.id(), id, request.quantity()));
    }

    @DeleteMapping("/api/cart/{id}")
    public ResponseEntity<CartResponse> remove(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id) {
        return ResponseEntity.ok(cartService.remove(user.id(), id));
    }

    @PostMapping("/api/cart/checkout")
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal final LoginUser user,
                                                  @RequestBody @Valid final CartCheckoutRequest request) {
        OrderResponse order = cartService.checkout(user.id(), request);
        return ResponseEntity.created(URI.create("/api/orders/" + order.id())).body(order);
    }

    @PostMapping("/api/orders/{orderId}/items/{itemId}/cancel")
    public ResponseEntity<OrderResponse> cancelMyItem(@AuthenticationPrincipal final LoginUser user,
                                                      @PathVariable final Long orderId, @PathVariable final Long itemId) {
        return ResponseEntity.ok(orderService.cancelMyItem(user.id(), orderId, itemId));
    }

    @PostMapping("/api/admin/orders/{orderId}/items/{itemId}/cancel")
    public ResponseEntity<OrderResponse> cancelItem(@PathVariable final Long orderId, @PathVariable final Long itemId) {
        return ResponseEntity.ok(orderService.cancelItem(orderId, itemId));
    }

    @PutMapping("/api/admin/orders/{orderId}/shipment")
    public ResponseEntity<OrderResponse> ship(@PathVariable final Long orderId, @RequestBody @Valid final ShipmentRequest request) {
        return ResponseEntity.ok(orderService.ship(orderId, request));
    }

    @GetMapping("/api/shipping-policy")
    public ResponseEntity<ShippingPolicyResponse> shippingPolicy() {
        return ResponseEntity.ok(shippingService.getPolicy());
    }

    @PutMapping("/api/admin/shipping-policy")
    public ResponseEntity<ShippingPolicyResponse> updateShippingPolicy(@RequestBody @Valid final ShippingPolicyRequest request) {
        return ResponseEntity.ok(shippingService.update(request));
    }
}

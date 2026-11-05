package com.dgmoonlabs.cms.domain.business.wallet.controller;

import com.dgmoonlabs.cms.domain.business.wallet.dto.WalletDtos.GiftCardResponse;
import com.dgmoonlabs.cms.domain.business.wallet.dto.WalletDtos.IssueGiftCodeRequest;
import com.dgmoonlabs.cms.domain.business.wallet.dto.WalletDtos.LedgerResponse;
import com.dgmoonlabs.cms.domain.business.wallet.dto.WalletDtos.RedeemRequest;
import com.dgmoonlabs.cms.domain.business.wallet.service.GiftCardService;
import com.dgmoonlabs.cms.domain.business.wallet.service.PrepaidService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final PrepaidService prepaidService;
    private final GiftCardService giftCardService;

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> balance(@AuthenticationPrincipal final LoginUser user) {
        return ResponseEntity.ok(prepaidService.balance(user.id()));
    }

    @GetMapping("/ledger")
    public ResponseEntity<PagedModel<LedgerResponse>> ledger(@AuthenticationPrincipal final LoginUser user,
                                                             @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(prepaidService.history(user.id(), pageable)));
    }

    @PostMapping("/gift-codes")
    public ResponseEntity<GiftCardResponse> issueCode(@AuthenticationPrincipal final LoginUser user,
                                                       @RequestBody @Valid final IssueGiftCodeRequest request) {
        return ResponseEntity.ok(giftCardService.issueCode(user.id(), request.amount()));
    }

    @GetMapping("/gift-codes")
    public ResponseEntity<PagedModel<GiftCardResponse>> myCodes(@AuthenticationPrincipal final LoginUser user,
                                                                 @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(giftCardService.getMine(user.id(), pageable)));
    }

    @PostMapping("/redeem")
    public ResponseEntity<GiftCardResponse> redeem(@AuthenticationPrincipal final LoginUser user,
                                                    @RequestBody @Valid final RedeemRequest request) {
        return ResponseEntity.ok(giftCardService.redeem(user.id(), request.code()));
    }
}

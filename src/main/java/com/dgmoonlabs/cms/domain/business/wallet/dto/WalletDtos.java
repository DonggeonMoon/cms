package com.dgmoonlabs.cms.domain.business.wallet.dto;

import com.dgmoonlabs.cms.domain.business.wallet.constant.GiftCardStatus;
import com.dgmoonlabs.cms.domain.business.wallet.constant.LedgerType;
import com.dgmoonlabs.cms.domain.business.wallet.entity.GiftCard;
import com.dgmoonlabs.cms.domain.business.wallet.entity.PrepaidLedger;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class WalletDtos {
    private WalletDtos() {
    }

    public record IssueGiftCodeRequest(@Positive BigDecimal amount) {
    }

    public record RedeemRequest(@NotBlank String code) {
    }

    public record LedgerResponse(Long id, LedgerType type, BigDecimal amount, BigDecimal balanceAfter, String memo,
                                 LocalDateTime createdAt) {
        public static LedgerResponse from(final PrepaidLedger ledger) {
            return new LedgerResponse(ledger.getId(), ledger.getType(), ledger.getAmount(), ledger.getBalanceAfter(),
                    ledger.getMemo(), ledger.getCreatedAt());
        }
    }

    public record GiftCardResponse(Long id, String code, BigDecimal amount, GiftCardStatus status, LocalDateTime redeemedAt,
                                   LocalDateTime createdAt) {
        public static GiftCardResponse from(final GiftCard card) {
            return new GiftCardResponse(card.getId(), card.getCode(), card.getAmount(), card.getStatus(), card.getRedeemedAt(),
                    card.getCreatedAt());
        }
    }
}

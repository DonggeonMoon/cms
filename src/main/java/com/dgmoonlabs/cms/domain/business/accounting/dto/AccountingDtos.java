package com.dgmoonlabs.cms.domain.business.accounting.dto;

import com.dgmoonlabs.cms.domain.business.accounting.entity.Receivable;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class AccountingDtos {
    private AccountingDtos() {
    }

    public record ReceivableCreateRequest(@NotNull Long memberId, Long orderId, @NotNull @Positive BigDecimal totalAmount,
                                          String note) {
    }

    public record CollectRequest(@NotNull @Positive BigDecimal amount) {
    }

    public record ReceivableResponse(Long id, Long memberId, Long orderId, BigDecimal totalAmount, BigDecimal collectedAmount,
                                     BigDecimal remaining, boolean settled, String note, LocalDateTime createdAt) {
        public static ReceivableResponse from(final Receivable entity) {
            return new ReceivableResponse(entity.getId(), entity.getMemberId(), entity.getOrderId(), entity.getTotalAmount(),
                    entity.getCollectedAmount(), entity.remaining(), entity.isSettled(), entity.getNote(), entity.getCreatedAt());
        }
    }

    public record SalesReportRow(LocalDate date, long count, BigDecimal amount) {
    }

    public record SettlementRow(PaymentMethod method, long count, BigDecimal amount) {
    }
}

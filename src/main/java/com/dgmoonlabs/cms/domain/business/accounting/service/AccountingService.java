package com.dgmoonlabs.cms.domain.business.accounting.service;

import com.dgmoonlabs.cms.domain.business.accounting.dto.AccountingDtos.CollectRequest;
import com.dgmoonlabs.cms.domain.business.accounting.dto.AccountingDtos.ReceivableCreateRequest;
import com.dgmoonlabs.cms.domain.business.accounting.dto.AccountingDtos.ReceivableResponse;
import com.dgmoonlabs.cms.domain.business.accounting.dto.AccountingDtos.SalesReportRow;
import com.dgmoonlabs.cms.domain.business.accounting.dto.AccountingDtos.SettlementRow;
import com.dgmoonlabs.cms.domain.business.accounting.entity.Receivable;
import com.dgmoonlabs.cms.domain.business.accounting.repository.ReceivableRepository;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentMethod;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentStatus;
import com.dgmoonlabs.cms.domain.business.payment.entity.Payment;
import com.dgmoonlabs.cms.domain.business.payment.repository.PaymentRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** 회계: 결제수단별 정산, 외상(선출고 후결제) 미수금, 기간별 매출 리포트. 집계는 전부 Payment 승인일시 기준(환불분은 그만큼 뺀 순액)이다. */
@Service
@RequiredArgsConstructor
public class AccountingService {
    private final ReceivableRepository receivableRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public ReceivableResponse createReceivable(final ReceivableCreateRequest request) {
        Receivable saved = receivableRepository.save(Receivable.builder()
                .memberId(request.memberId())
                .orderId(request.orderId())
                .totalAmount(request.totalAmount())
                .note(request.note())
                .build());
        return ReceivableResponse.from(saved);
    }

    @Transactional
    public ReceivableResponse collect(final Long id, final CollectRequest request) {
        Receivable receivable = receivableRepository.findById(id).orElseThrow(BusinessException::notFound);
        receivable.collect(request.amount());
        return ReceivableResponse.from(receivable);
    }

    @Transactional(readOnly = true)
    public Page<ReceivableResponse> search(final Long memberId, final Pageable pageable) {
        Page<Receivable> page = memberId == null ? receivableRepository.findAllByOrderByIdDesc(pageable)
                : receivableRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable);
        return page.map(ReceivableResponse::from);
    }

    @Transactional(readOnly = true)
    public List<SalesReportRow> salesReport(final LocalDate from, final LocalDate to) {
        List<Payment> payments = approvedBetween(from, to);
        Map<LocalDate, List<Payment>> byDate = new TreeMap<>();
        for (Payment payment : payments) {
            byDate.computeIfAbsent(payment.getApprovedAt().toLocalDate(), key -> new java.util.ArrayList<>()).add(payment);
        }
        return byDate.entrySet().stream()
                .map(entry -> new SalesReportRow(entry.getKey(), entry.getValue().size(), netAmountOf(entry.getValue())))
                .sorted(Comparator.comparing(SalesReportRow::date))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SettlementRow> settlementSummary(final LocalDate from, final LocalDate to) {
        List<Payment> payments = approvedBetween(from, to);
        Map<PaymentMethod, List<Payment>> byMethod = new EnumMap<>(PaymentMethod.class);
        for (Payment payment : payments) {
            byMethod.computeIfAbsent(payment.getMethod(), key -> new java.util.ArrayList<>()).add(payment);
        }
        return byMethod.entrySet().stream()
                .map(entry -> new SettlementRow(entry.getKey(), entry.getValue().size(), netAmountOf(entry.getValue())))
                .toList();
    }

    private List<Payment> approvedBetween(final LocalDate from, final LocalDate to) {
        return paymentRepository.findAllByApprovedAtBetweenOrderByApprovedAtAsc(
                from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    /** 전액 환불(취소)은 refundedAmount가 안 채워질 수 있어(결제 취소는 payment.cancel()만 호출) 상태로도 한 번 더 걸러낸다. */
    private static BigDecimal netAmountOf(final List<Payment> payments) {
        return payments.stream()
                .map(payment -> payment.getStatus() == PaymentStatus.CANCELED ? BigDecimal.ZERO
                        : payment.getAmount().subtract(payment.getRefundedAmount() == null ? BigDecimal.ZERO : payment.getRefundedAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

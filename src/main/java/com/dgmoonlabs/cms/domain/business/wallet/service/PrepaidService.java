package com.dgmoonlabs.cms.domain.business.wallet.service;

import com.dgmoonlabs.cms.domain.business.wallet.constant.LedgerType;
import com.dgmoonlabs.cms.domain.business.wallet.dto.WalletDtos.LedgerResponse;
import com.dgmoonlabs.cms.domain.business.wallet.entity.PrepaidLedger;
import com.dgmoonlabs.cms.domain.business.wallet.repository.PrepaidLedgerRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PrepaidService {
    private final PrepaidLedgerRepository ledgerRepository;

    @Transactional(readOnly = true)
    public BigDecimal balance(final Long memberId) {
        return ledgerRepository.balanceOf(memberId);
    }

    @Transactional(readOnly = true)
    public Page<LedgerResponse> history(final Long memberId, final Pageable pageable) {
        return ledgerRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable).map(LedgerResponse::from);
    }

    @Transactional
    public void credit(final Long memberId, final BigDecimal amount, final LedgerType type, final Long orderId, final String memo) {
        if (amount.signum() <= 0) {
            return;
        }
        record(memberId, type, amount, orderId, memo);
    }

    @Transactional
    public void use(final Long memberId, final BigDecimal amount, final LedgerType type, final Long orderId, final String memo) {
        if (amount.signum() <= 0) {
            return;
        }
        if (balance(memberId).compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.PREPAID_BALANCE_EXCEEDED);
        }
        record(memberId, type, amount.negate(), orderId, memo);
    }

    private void record(final Long memberId, final LedgerType type, final BigDecimal amount, final Long orderId, final String memo) {
        ledgerRepository.save(PrepaidLedger.builder()
                .memberId(memberId)
                .type(type)
                .amount(amount)
                .balanceAfter(balance(memberId).add(amount))
                .orderId(orderId)
                .memo(memo)
                .build());
    }
}

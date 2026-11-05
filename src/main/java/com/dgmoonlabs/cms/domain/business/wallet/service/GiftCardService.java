package com.dgmoonlabs.cms.domain.business.wallet.service;

import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.business.wallet.constant.GiftCardStatus;
import com.dgmoonlabs.cms.domain.business.wallet.constant.LedgerType;
import com.dgmoonlabs.cms.domain.business.wallet.dto.WalletDtos.GiftCardResponse;
import com.dgmoonlabs.cms.domain.business.wallet.entity.GiftCard;
import com.dgmoonlabs.cms.domain.business.wallet.repository.GiftCardRepository;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GiftCardService {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GiftCardRepository giftCardRepository;
    private final OrderRepository orderRepository;
    private final PrepaidService prepaidService;

    /** 상품권 상품을 결제하면 산 사람 지갑에 바로 충전된다(선불 충전). 따로 코드로 바꿔 선물할 수도 있다. */
    @EventListener
    @Transactional
    public void chargeOnPayment(final OrderPaidEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null || order.getSaleType() != ProductSaleType.GIFT_CARD || order.getUserType() != UserType.MEMBER) {
            return;
        }
        prepaidService.credit(order.getUserId(), order.getTotalPrice(), LedgerType.CHARGE, order.getId(), "상품권 구매 충전");
    }

    @Transactional
    public GiftCardResponse issueCode(final Long memberId, final BigDecimal amount) {
        prepaidService.use(memberId, amount, LedgerType.GIFT_ISSUED, null, "상품권 발급");
        GiftCard card = giftCardRepository.save(GiftCard.builder()
                .code(nextCode())
                .amount(amount)
                .issuedMemberId(memberId)
                .status(GiftCardStatus.UNUSED)
                .build());
        return GiftCardResponse.from(card);
    }

    @Transactional
    public GiftCardResponse redeem(final Long memberId, final String code) {
        GiftCard card = giftCardRepository.findByCode(code.trim().toUpperCase()).orElseThrow(BusinessException::notFound);
        if (card.getStatus() != GiftCardStatus.UNUSED) {
            throw new BusinessException(ErrorCode.GIFT_CARD_ALREADY_USED);
        }
        card.redeem(memberId, LocalDateTime.now());
        prepaidService.credit(memberId, card.getAmount(), LedgerType.GIFT_REDEEMED, null, "상품권(" + card.getCode() + ") 충전");
        return GiftCardResponse.from(card);
    }

    @Transactional(readOnly = true)
    public Page<GiftCardResponse> getMine(final Long memberId, final Pageable pageable) {
        return giftCardRepository.findAllByIssuedMemberIdOrderByIdDesc(memberId, pageable).map(GiftCardResponse::from);
    }

    private String nextCode() {
        String code;
        do {
            StringBuilder builder = new StringBuilder(12);
            for (int i = 0; i < 12; i++) {
                builder.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
            }
            code = builder.toString();
        } while (giftCardRepository.existsByCode(code));
        return code;
    }
}

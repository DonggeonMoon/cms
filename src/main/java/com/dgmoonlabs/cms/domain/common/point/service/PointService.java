package com.dgmoonlabs.cms.domain.common.point.service;

import com.dgmoonlabs.cms.domain.common.point.constant.PointType;
import com.dgmoonlabs.cms.domain.common.point.dto.PointDtos.*;
import com.dgmoonlabs.cms.domain.common.point.entity.PointLedger;
import com.dgmoonlabs.cms.domain.common.point.entity.PointPolicy;
import com.dgmoonlabs.cms.domain.common.point.repository.PointLedgerRepository;
import com.dgmoonlabs.cms.domain.common.point.repository.PointPolicyRepository;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {
    private static final int EXPIRING_SOON_DAYS = 30;

    private final PointPolicyRepository policyRepository;
    private final PointLedgerRepository ledgerRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public PointPolicyResponse getPolicy() {
        return PointPolicyResponse.from(policy());
    }

    @Transactional
    public PointPolicyResponse updatePolicy(final PointPolicyRequest request) {
        PointPolicy policy = policyRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> policyRepository.save(PointPolicy.builder().build()));
        policy.update(request.isEnabled(), request.earnRate(), request.expireDays(), request.minUsePoints(),
                request.maxUseRate(), request.signupPoints(), request.referralPoints());
        return PointPolicyResponse.from(policy);
    }

    @Transactional(readOnly = true)
    public long balance(final Long memberId) {
        return ledgerRepository.balanceOf(memberId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public PointSummary summary(final Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime by = now.plusDays(EXPIRING_SOON_DAYS);
        long soon = ledgerRepository.findExpiringBetween(memberId, now, by).stream()
                .mapToLong(PointLedger::getRemaining).sum();
        return new PointSummary(ledgerRepository.balanceOf(memberId, now), soon, by);
    }

    @Transactional(readOnly = true)
    public Page<PointLedgerResponse> history(final Long memberId, final Pageable pageable) {
        return ledgerRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable).map(PointLedgerResponse::from);
    }

    /** 주문에 쓸 수 있는 최대 포인트: 잔액과, 할인 뒤 상품 금액에 최대 사용 비율을 곱한 값 중 작은 쪽. */
    @Transactional(readOnly = true)
    public long maxUsable(final Long memberId, final BigDecimal itemsAfterCoupon) {
        PointPolicy policy = policy();
        if (!policy.isEnabled() || memberId == null) {
            return 0;
        }
        long cap = itemsAfterCoupon.multiply(BigDecimal.valueOf(policy.getMaxUseRate()))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).longValue();
        return Math.max(0, Math.min(balance(memberId), cap));
    }

    public void validateUse(final Long memberId, final long points, final BigDecimal itemsAfterCoupon) {
        if (points <= 0) {
            return;
        }
        if (points < policy().getMinUsePoints()) {
            throw new BusinessException(ErrorCode
                    .POINTS_BELOW_MINIMUM);
        }
        if (points > maxUsable(memberId, itemsAfterCoupon)) {
            throw new BusinessException(ErrorCode.POINTS_EXCEEDED);
        }
    }

    /** 소멸이 가까운 적립분부터 쓴다. */
    @Transactional
    public void use(final Long memberId, final long points, final Long orderId) {
        if (points <= 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        long left = points;
        for (PointLedger ledger : ledgerRepository.findUsableForUpdate(memberId, now)) {
            left -= ledger.take(left);
            if (left == 0) {
                break;
            }
        }
        if (left > 0) {
            throw new BusinessException(ErrorCode.POINTS_EXCEEDED);
        }
        record(memberId, PointType.USE, -points, 0, null, orderId, null);
    }

    @Transactional
    public void restore(final Long memberId, final long points, final Long orderId) {
        if (points > 0) {
            credit(memberId, PointType.RESTORE, points, orderId, null);
        }
    }

    @Transactional
    public void earn(final Long memberId, final long points, final Long orderId) {
        if (points > 0 && policy().isEnabled()) {
            credit(memberId, PointType.EARN, points, orderId, null);
        }
    }

    /** 취소된 주문의 적립분을 거둔다. 이미 써서 남은 포인트가 모자라면 남은 만큼만 거둔다. */
    @Transactional
    public long revokeEarned(final Long memberId, final long points, final Long orderId) {
        if (points <= 0) {
            return 0;
        }
        long taken = deduct(memberId, points);
        if (taken > 0) {
            record(memberId, PointType.REVOKE, -taken, 0, null, orderId, null);
        }
        return taken;
    }

    public long earnFor(final BigDecimal netAmount) {
        PointPolicy policy = policy();
        if (!policy.isEnabled() || netAmount.signum() <= 0) {
            return 0;
        }
        return netAmount.multiply(policy.getEarnRate()).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).longValue();
    }

    @Transactional
    public void grantSignup(final Long memberId) {
        PointPolicy policy = policy();
        if (policy.isEnabled() && policy.getSignupPoints() > 0) {
            credit(memberId, PointType.SIGNUP, policy.getSignupPoints(), null, null);
        }
    }

    /** 추천받은 회원이 첫 결제를 마치면 추천인에게 지급한다. */
    @Transactional
    public void grantReferral(final Long referrerMemberId, final String memo) {
        PointPolicy policy = policy();
        if (policy.isEnabled() && policy.getReferralPoints() > 0) {
            credit(referrerMemberId, PointType.REFERRAL, policy.getReferralPoints(), null, memo);
        }
    }

    @Transactional
    public void adjust(final Long memberId, final PointAdjustRequest request) {
        memberRepository.findByIdAndDeletedAtIsNull(memberId).orElseThrow(BusinessException::notFound);
        if (request.amount() > 0) {
            credit(memberId, PointType.ADMIN, request.amount(), null, request.memo());
        } else if (request.amount() < 0) {
            long wanted = -request.amount();
            if (wanted > balance(memberId)) {
                throw new BusinessException(ErrorCode.POINTS_EXCEEDED);
            }
            deduct(memberId, wanted);
            record(memberId, PointType.ADMIN, -wanted, 0, null, null, request.memo());
        }
    }

    @Scheduled(cron = "${cms.point.expire-cron:0 20 0 * * *}")
    public void expireOnSchedule() {
        int expired = expireDue(LocalDateTime.now());
        if (expired > 0) {
            log.info("Expired {} point entries", expired);
        }
    }

    @Transactional
    public int expireDue(final LocalDateTime now) {
        List<PointLedger> expired = ledgerRepository.findExpired(now);
        for (PointLedger ledger : expired) {
            long left = ledger.take(ledger.getRemaining());
            record(ledger.getMemberId(), PointType.EXPIRE, -left, 0, null, null, null);
        }
        return expired.size();
    }

    private void credit(final Long memberId, final PointType type, final long points, final Long orderId, final String memo) {
        record(memberId, type, points, points, LocalDateTime.now().plusDays(policy().getExpireDays()), orderId, memo);
    }

    private long deduct(final Long memberId, final long wanted) {
        long left = wanted;
        for (PointLedger ledger : ledgerRepository.findUsableForUpdate(memberId, LocalDateTime.now())) {
            left -= ledger.take(left);
            if (left == 0) {
                break;
            }
        }
        return wanted - left;
    }

    private void record(final Long memberId, final PointType type, final long amount, final long remaining,
                        final LocalDateTime expiresAt, final Long orderId, final String memo) {
        ledgerRepository.flush();
        long balance = ledgerRepository.balanceOf(memberId, LocalDateTime.now()) + remaining;
        ledgerRepository.save(PointLedger.builder()
                .memberId(memberId)
                .type(type)
                .amount(amount)
                .remaining(remaining)
                .expiresAt(expiresAt)
                .balanceAfter(balance)
                .orderId(orderId)
                .memo(memo)
                .build());
    }

    private PointPolicy policy() {
        return policyRepository.findFirstByOrderByIdAsc().orElseGet(() -> PointPolicy.builder().build());
    }
}

package com.dgmoonlabs.cms.domain.common.coupon.service;

import com.dgmoonlabs.cms.domain.common.coupon.constant.CouponIssueStatus;
import com.dgmoonlabs.cms.domain.common.coupon.constant.CouponIssueType;
import com.dgmoonlabs.cms.domain.common.coupon.constant.DiscountType;
import com.dgmoonlabs.cms.domain.common.coupon.dto.CouponDtos.*;
import com.dgmoonlabs.cms.domain.common.coupon.entity.Coupon;
import com.dgmoonlabs.cms.domain.common.coupon.entity.CouponIssue;
import com.dgmoonlabs.cms.domain.common.coupon.repository.CouponIssueRepository;
import com.dgmoonlabs.cms.domain.common.coupon.repository.CouponRepository;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponIssueRepository issueRepository;
    private final MemberRepository memberRepository;

    public record Line(Long productId, BigDecimal amount) {
    }

    /** 할인 합계와 상품 줄마다 나눈 할인액(적용 대상이 아닌 줄은 0). */
    public record Evaluation(BigDecimal discount, List<BigDecimal> allocation) {
        static Evaluation none(final int size) {
            return new Evaluation(BigDecimal.ZERO, Collections.nCopies(size, BigDecimal.ZERO));
        }
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> getCoupons() {
        return couponRepository.findAllByDeletedAtIsNullOrderByIdDesc().stream().map(CouponResponse::from).toList();
    }

    @Transactional
    public CouponResponse create(final CouponSaveRequest request) {
        validate(request);
        Coupon coupon = couponRepository.save(Coupon.builder()
                .name(request.name()).discountType(request.discountType()).discountValue(request.discountValue())
                .issueType(request.issueType()).build());
        apply(coupon, request);
        return CouponResponse.from(coupon);
    }

    @Transactional
    public CouponResponse update(final Long id, final CouponSaveRequest request) {
        validate(request);
        Coupon coupon = findCoupon(id);
        apply(coupon, request);
        return CouponResponse.from(coupon);
    }

    @Transactional
    public void delete(final Long id) {
        findCoupon(id).delete();
    }

    @Transactional(readOnly = true)
    public Page<CouponIssueResponse> issuesOf(final Long couponId, final Pageable pageable) {
        Coupon coupon = findCoupon(couponId);
        Page<CouponIssue> issues = issueRepository.findAllByCouponIdOrderByIdDesc(couponId, pageable);
        Map<Long, String> usernames = memberRepository.findAllById(issues.map(CouponIssue::getMemberId).toList()).stream()
                .collect(Collectors.toMap(Member::getId, Member::getUsername));
        return issues.map(issue -> CouponIssueResponse.of(issue, coupon, usernames.get(issue.getMemberId())));
    }

    @Transactional
    public IssueResult issueByAdmin(final Long couponId, final IssueRequest request) {
        Coupon coupon = couponRepository.findForUpdate(couponId).filter(found -> found.getDeletedAt() == null)
                .orElseThrow(BusinessException::notFound);
        List<String> skipped = new ArrayList<>();
        int issued = 0;
        for (String username : new LinkedHashSet<>(request.usernames().stream().map(String::trim).toList())) {
            Optional<Member> member = memberRepository.findByUsernameAndDeletedAtIsNull(username);
            if (member.isEmpty() || coupon.isSoldOut()) {
                skipped.add(username);
                continue;
            }
            issue(coupon, member.get().getId());
            issued++;
        }
        return new IssueResult(issued, skipped);
    }

    @Transactional(readOnly = true)
    public List<DownloadableCoupon> downloadable(final Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findAllByIssueTypeAndDeletedAtIsNull(CouponIssueType.DOWNLOAD).stream()
                .filter(coupon -> coupon.isEnabled() && (coupon.getIssueEndAt() == null || coupon.getIssueEndAt().isAfter(now))
                        && (coupon.getIssueStartAt() == null || !coupon.getIssueStartAt().isAfter(now)))
                .map(coupon -> new DownloadableCoupon(CouponResponse.from(coupon),
                        memberId != null && issueRepository.existsByCouponIdAndMemberId(coupon.getId(), memberId),
                        coupon.isSoldOut()))
                .toList();
    }

    @Transactional
    public CouponIssueResponse download(final Long memberId, final Long couponId) {
        Coupon coupon = couponRepository.findForUpdate(couponId)
                .filter(found -> found.getIssueType() == CouponIssueType.DOWNLOAD)
                .orElseThrow(BusinessException::notFound);
        if (!coupon.isIssuableAt(LocalDateTime.now())) {
            throw new BusinessException(coupon.isSoldOut() ? ErrorCode.COUPON_SOLD_OUT : ErrorCode.COUPON_NOT_DOWNLOADABLE);
        }
        if (issueRepository.existsByCouponIdAndMemberId(couponId, memberId)) {
            throw new BusinessException(ErrorCode.COUPON_ALREADY_ISSUED);
        }
        return CouponIssueResponse.of(issue(coupon, memberId), coupon, null);
    }

    @Transactional(readOnly = true)
    public List<CouponIssueResponse> mine(final Long memberId) {
        List<CouponIssue> issues = issueRepository.findAllByMemberIdOrderByIdDesc(memberId);
        Map<Long, Coupon> coupons = couponsOf(issues);
        LocalDateTime now = LocalDateTime.now();
        return issues.stream()
                .map(issue -> CouponIssueResponse.of(issue.getStatus() == CouponIssueStatus.AVAILABLE && !issue.isUsableAt(now)
                        ? expired(issue) : issue, coupons.get(issue.getCouponId()), null))
                .toList();
    }

    /** 가입·등급 달성처럼 조건이 맞을 때 자동으로 주는 쿠폰. 한 사람에게 한 번만 준다. */
    @Transactional
    public void issueAutomatically(final Long memberId, final CouponIssueType type, final Long gradeId) {
        LocalDateTime now = LocalDateTime.now();
        for (Coupon candidate : couponRepository.findAllByIssueTypeAndDeletedAtIsNull(type)) {
            if (type == CouponIssueType.GRADE && !Objects.equals(candidate.getTargetGradeId(), gradeId)) {
                continue;
            }
            Coupon coupon = couponRepository.findForUpdate(candidate.getId()).orElseThrow();
            if (coupon.isIssuableAt(now) && !issueRepository.existsByCouponIdAndMemberId(coupon.getId(), memberId)) {
                issue(coupon, memberId);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<CouponOption> optionsFor(final Long memberId, final List<Line> lines) {
        List<CouponIssue> issues = issueRepository.findAllByMemberIdOrderByIdDesc(memberId);
        Map<Long, Coupon> coupons = couponsOf(issues);
        LocalDateTime now = LocalDateTime.now();
        return issues.stream().filter(issue -> issue.isUsableAt(now)).map(issue -> {
            Coupon coupon = coupons.get(issue.getCouponId());
            BigDecimal discount = coupon == null ? BigDecimal.ZERO : evaluate(coupon, lines).discount();
            return new CouponOption(issue.getId(), coupon == null ? "삭제된 쿠폰" : coupon.getName(), discount,
                    discount.signum() > 0, issue.getExpiresAt());
        }).toList();
    }

    @Transactional(readOnly = true)
    public Evaluation evaluate(final Long memberId, final Long issueId, final List<Line> lines) {
        if (issueId == null) {
            return Evaluation.none(lines.size());
        }
        CouponIssue issue = issueRepository.findByIdAndMemberId(issueId, memberId)
                .filter(found -> found.isUsableAt(LocalDateTime.now()))
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_USABLE));
        Coupon coupon = couponRepository.findById(issue.getCouponId()).orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_USABLE));
        Evaluation evaluation = evaluate(coupon, lines);
        if (evaluation.discount().signum() <= 0) {
            throw new BusinessException(ErrorCode.COUPON_CONDITION_NOT_MET);
        }
        return evaluation;
    }

    @Transactional
    public void use(final Long memberId, final Long issueId, final Long orderId) {
        issueRepository.findByIdAndMemberId(issueId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_USABLE)).use(orderId);
    }

    @Transactional
    public void restore(final Long issueId) {
        if (issueId != null) {
            issueRepository.findById(issueId).ifPresent(CouponIssue::restore);
        }
    }

    @Scheduled(cron = "${cms.coupon.expire-cron:0 30 0 * * *}")
    @Transactional
    public void expireOnSchedule() {
        issueRepository.findExpired(CouponIssueStatus.AVAILABLE, LocalDateTime.now()).forEach(CouponIssue::expire);
    }

    /** 대상 상품 줄의 금액 합으로 할인을 정하고, 줄 금액 비율로 나눈다. 나머지는 마지막 대상 줄이 가진다. */
    private static Evaluation evaluate(final Coupon coupon, final List<Line> lines) {
        List<Integer> targets = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if (coupon.getProductId() == null || coupon.getProductId().equals(lines.get(i).productId())) {
                targets.add(i);
            }
        }
        BigDecimal base = targets.stream().map(i -> lines.get(i).amount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = coupon.discountFor(base);
        List<BigDecimal> allocation = new ArrayList<>(Collections.nCopies(lines.size(), BigDecimal.ZERO));
        if (discount.signum() <= 0) {
            return new Evaluation(BigDecimal.ZERO, allocation);
        }
        BigDecimal left = discount;
        for (int k = 0; k < targets.size(); k++) {
            int i = targets.get(k);
            BigDecimal share = k == targets.size() - 1 ? left
                    : discount.multiply(lines.get(i).amount()).divide(base, 0, RoundingMode.DOWN);
            allocation.set(i, share);
            left = left.subtract(share);
        }
        return new Evaluation(discount, allocation);
    }

    private CouponIssue issue(final Coupon coupon, final Long memberId) {
        coupon.countIssue();
        return issueRepository.save(CouponIssue.builder()
                .couponId(coupon.getId())
                .memberId(memberId)
                .expiresAt(coupon.expiryFrom(LocalDateTime.now()))
                .build());
    }

    private static CouponIssue expired(final CouponIssue issue) {
        issue.expire();
        return issue;
    }

    private Map<Long, Coupon> couponsOf(final List<CouponIssue> issues) {
        return couponRepository.findAllById(issues.stream().map(CouponIssue::getCouponId).distinct().toList()).stream()
                .collect(Collectors.toMap(Coupon::getId, Function.identity()));
    }

    private static void validate(final CouponSaveRequest request) {
        if (request.discountType() == DiscountType.RATE
                && request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (request.issueType() == CouponIssueType.GRADE && request.targetGradeId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private static void apply(final Coupon coupon, final CouponSaveRequest request) {
        coupon.update(request.name().trim(), request.description(), request.discountType(), request.discountValue(),
                request.maxDiscountAmount(), request.minOrderAmount(), request.productId(), request.issueType(),
                request.issueType() == CouponIssueType.GRADE ? request.targetGradeId() : null, request.issueStartAt(),
                request.issueEndAt(), request.validDays(), request.validUntil(), request.totalQuantity(),
                request.isEnabledOrDefault());
    }

    private Coupon findCoupon(final Long id) {
        return couponRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }
}

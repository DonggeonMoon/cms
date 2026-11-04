package com.dgmoonlabs.cms.domain.common.coupon.dto;

import com.dgmoonlabs.cms.domain.common.coupon.constant.CouponIssueStatus;
import com.dgmoonlabs.cms.domain.common.coupon.constant.CouponIssueType;
import com.dgmoonlabs.cms.domain.common.coupon.constant.DiscountType;
import com.dgmoonlabs.cms.domain.common.coupon.entity.Coupon;
import com.dgmoonlabs.cms.domain.common.coupon.entity.CouponIssue;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class CouponDtos {
    private CouponDtos() {
    }

    public record CouponSaveRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 255) String description,
            @NotNull DiscountType discountType,
            @NotNull @Positive BigDecimal discountValue,
            @PositiveOrZero BigDecimal maxDiscountAmount,
            @PositiveOrZero BigDecimal minOrderAmount,
            Long productId,
            @NotNull CouponIssueType issueType,
            Long targetGradeId,
            LocalDateTime issueStartAt,
            LocalDateTime issueEndAt,
            @Positive @Max(3650) Integer validDays,
            LocalDateTime validUntil,
            @Positive Integer totalQuantity,
            Boolean isEnabled
    ) {
        public boolean isEnabledOrDefault() {
            return isEnabled == null || isEnabled;
        }
    }

    public record CouponResponse(Long id, String name, String description, DiscountType discountType, BigDecimal discountValue,
                                 BigDecimal maxDiscountAmount, BigDecimal minOrderAmount, Long productId,
                                 CouponIssueType issueType, Long targetGradeId, LocalDateTime issueStartAt,
                                 LocalDateTime issueEndAt, Integer validDays, LocalDateTime validUntil,
                                 Integer totalQuantity, int issuedCount, boolean isEnabled) {
        public static CouponResponse from(final Coupon coupon) {
            return new CouponResponse(coupon.getId(), coupon.getName(), coupon.getDescription(), coupon.getDiscountType(),
                    coupon.getDiscountValue(), coupon.getMaxDiscountAmount(), coupon.getMinOrderAmount(), coupon.getProductId(),
                    coupon.getIssueType(), coupon.getTargetGradeId(), coupon.getIssueStartAt(), coupon.getIssueEndAt(),
                    coupon.getValidDays(), coupon.getValidUntil(), coupon.getTotalQuantity(), coupon.getIssuedCount(),
                    coupon.isEnabled());
        }
    }

    public record DownloadableCoupon(CouponResponse coupon, boolean downloaded, boolean soldOut) {
    }

    public record IssueRequest(@NotEmpty @Size(max = 500) List<@NotBlank String> usernames) {
    }

    public record IssueResult(int issued, List<String> skipped) {
    }

    public record CouponIssueResponse(Long id, Long couponId, Long memberId, String username, String couponName,
                                      DiscountType discountType, BigDecimal discountValue, BigDecimal maxDiscountAmount,
                                      BigDecimal minOrderAmount, Long productId, CouponIssueStatus status,
                                      LocalDateTime expiresAt, Long orderId, LocalDateTime usedAt, LocalDateTime createdAt) {
        public static CouponIssueResponse of(final CouponIssue issue, final Coupon coupon, final String username) {
            return new CouponIssueResponse(issue.getId(), issue.getCouponId(), issue.getMemberId(), username,
                    coupon == null ? "삭제된 쿠폰" : coupon.getName(),
                    coupon == null ? null : coupon.getDiscountType(), coupon == null ? null : coupon.getDiscountValue(),
                    coupon == null ? null : coupon.getMaxDiscountAmount(), coupon == null ? null : coupon.getMinOrderAmount(),
                    coupon == null ? null : coupon.getProductId(), issue.getStatus(), issue.getExpiresAt(),
                    issue.getOrderId(), issue.getUsedAt(), issue.getCreatedAt());
        }
    }

    public record CouponOption(Long issueId, String name, BigDecimal discount, boolean applicable, LocalDateTime expiresAt) {
    }
}

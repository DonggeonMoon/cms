package com.dgmoonlabs.cms.domain.business.shipping.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ShippingPolicy extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    @ColumnDefault("0")
    @Comment("기본 배송비")
    private BigDecimal baseFee = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    @Comment("무료 배송 기준 금액 (이상이면 기본 배송비 면제, null: 없음)")
    private BigDecimal freeThreshold;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("지역별 추가 배송비 (우편번호 범위)")
    private List<RegionFee> regionFees;

    @Column(length = 50)
    @Comment("기본 택배사")
    private String defaultCourier;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("해외 배송 나라별 배송비 (등록되지 않은 나라는 주문 불가)")
    private List<CountryFee> internationalFees;

    public record RegionFee(String name, String postalCodeFrom, String postalCodeTo, BigDecimal fee) {
        public boolean covers(final String postalCode) {
            return postalCode.compareTo(postalCodeFrom) >= 0 && postalCode.compareTo(postalCodeTo) <= 0;
        }
    }

    public record CountryFee(String country, BigDecimal fee) {
    }

    public void update(final BigDecimal baseFee, final BigDecimal freeThreshold, final List<RegionFee> regionFees,
                       final String defaultCourier, final List<CountryFee> internationalFees) {
        this.baseFee = baseFee;
        this.freeThreshold = freeThreshold;
        this.regionFees = regionFees;
        this.defaultCourier = defaultCourier;
        this.internationalFees = internationalFees;
    }

    /** 무료 배송 기준은 기본 배송비에만 적용하고, 제주·도서산간 같은 지역 추가 배송비는 늘 받는다. 해외는 국내 규칙과 무관하게 나라별 고정 배송비만 받는다. */
    public Quote quote(final BigDecimal subtotal, final String postalCode, final String country) {
        if (country != null && !country.isBlank() && !country.equalsIgnoreCase("KR")) {
            CountryFee match = internationalFees == null ? null
                    : internationalFees.stream().filter(fee -> fee.country().equalsIgnoreCase(country.trim())).findFirst()
                            .orElseThrow(() -> new BusinessException(ErrorCode.COUNTRY_NOT_SUPPORTED));
            return new Quote(match.fee(), BigDecimal.ZERO, null, match.fee());
        }
        BigDecimal base = freeThreshold != null && subtotal.compareTo(freeThreshold) >= 0 ? BigDecimal.ZERO : baseFee;
        String code = postalCode == null ? "" : postalCode.replaceAll("\\D", "");
        RegionFee region = code.length() != 5 || regionFees == null ? null
                : regionFees.stream().filter(fee -> fee.covers(code)).findFirst().orElse(null);
        BigDecimal extra = region == null ? BigDecimal.ZERO : region.fee();
        return new Quote(base, extra, region == null ? null : region.name(), base.add(extra));
    }

    public record Quote(BigDecimal baseFee, BigDecimal regionFee, String regionName, BigDecimal total) {
    }
}

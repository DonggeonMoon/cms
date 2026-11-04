package com.dgmoonlabs.cms.domain.business.order.entity.product;

import com.dgmoonlabs.cms.domain.business.order.constant.BillingCycle;
import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
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
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("상품명")
    private String name;

    @Comment("상품 설명")
    private String description;

    @Comment("대표 이미지 파일 ID")
    private Long imageFileId;

    @Column(nullable = false, precision = 15, scale = 2)
    @Comment("가격")
    private BigDecimal price;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @ColumnDefault("'PURCHASE'")
    @Enumerated(EnumType.STRING)
    @Comment("판매 방식")
    private ProductSaleType saleType = ProductSaleType.PURCHASE;

    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("구독 결제 주기")
    private BillingCycle billingCycle;

    @Column(length = 200)
    @Comment("후원 추천 금액 (쉼표로 구분)")
    private String donationPresets;

    @Column(precision = 15, scale = 2)
    @Comment("후원 목표 금액")
    private BigDecimal goalAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("옵션 그룹 (이름과 값 목록). 비어 있으면 단일 옵션 목록")
    private List<OptionGroup> optionGroups;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("배송이 필요한 상품인지 (디지털·서비스 상품은 false)")
    private boolean requiresShipping = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("결제 후 내려받을 디지털 파일 ID 목록")
    private List<Long> digitalFileIds;

    @Comment("디지털 파일 내려받기 횟수 제한 (구매 한 건당, 없으면 무제한)")
    private Integer downloadLimit;

    @Comment("디지털 파일 내려받기 가능 일수 (결제일부터, 없으면 무기한)")
    private Integer downloadDays;

    @Comment("구매 가능 등급 ID (없으면 누구나)")
    private Long accessGradeId;

    @Comment("결제하면 부여할 등급 ID")
    private Long grantGradeId;

    @Comment("부여한 등급 유지 일수 (구매 상품, 없으면 무기한)")
    private Integer grantGradeDays;

    @Comment("재고 수량 (null: 무제한)")
    private Integer availableQuantity;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 텍스트")
    private String contentText;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 HTML")
    private String contentHtml;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("판매 여부")
    private boolean isEnabled;

    public void update(final String name, final String description, final Long imageFileId, final BigDecimal price,
                       final Integer availableQuantity, final String contentHtml, final String contentText,
                       final boolean isEnabled) {
        this.name = name;
        this.description = description;
        this.imageFileId = imageFileId;
        this.price = price;
        this.availableQuantity = availableQuantity;
        this.contentHtml = contentHtml;
        this.contentText = contentText;
        this.isEnabled = isEnabled;
    }

    public void changeSaleType(final ProductSaleType saleType, final BillingCycle billingCycle, final String donationPresets,
                               final BigDecimal goalAmount) {
        this.saleType = saleType;
        this.billingCycle = saleType == ProductSaleType.SUBSCRIPTION ? billingCycle : null;
        this.donationPresets = saleType == ProductSaleType.DONATION ? donationPresets : null;
        this.goalAmount = saleType == ProductSaleType.DONATION ? goalAmount : null;
    }

    public record OptionGroup(String name, List<String> values) {
    }

    public void changeOptionGroups(final List<OptionGroup> optionGroups) {
        this.optionGroups = optionGroups == null || optionGroups.isEmpty() ? null : optionGroups;
    }

    public boolean hasOptionGroups() {
        return optionGroups != null && !optionGroups.isEmpty();
    }

    public void changeDigitalFiles(final List<Long> fileIds, final Integer downloadLimit, final Integer downloadDays) {
        boolean digital = fileIds != null && !fileIds.isEmpty();
        this.digitalFileIds = digital ? List.copyOf(fileIds) : null;
        this.downloadLimit = digital ? downloadLimit : null;
        this.downloadDays = digital ? downloadDays : null;
    }

    public boolean hasDigitalFiles() {
        return digitalFileIds != null && !digitalFileIds.isEmpty();
    }

    public void changeShipping(final boolean requiresShipping) {
        this.requiresShipping = requiresShipping;
    }

    public void changeGrades(final Long accessGradeId, final Long grantGradeId, final Integer grantGradeDays) {
        this.accessGradeId = accessGradeId;
        this.grantGradeId = grantGradeId;
        this.grantGradeDays = grantGradeId == null || saleType == ProductSaleType.SUBSCRIPTION ? null : grantGradeDays;
    }

    public void decreaseStock(final int quantity) {
        if (availableQuantity == null) {
            return;
        }
        if (availableQuantity < quantity) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
        availableQuantity -= quantity;
    }

    public void increaseStock(final int quantity) {
        if (availableQuantity != null) {
            availableQuantity += quantity;
        }
    }

    public boolean isOnSale() {
        return isEnabled && getDeletedAt() == null;
    }
}

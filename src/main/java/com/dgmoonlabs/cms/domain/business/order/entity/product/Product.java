package com.dgmoonlabs.cms.domain.business.order.entity.product;

import com.dgmoonlabs.cms.domain.business.order.constant.BillingCycle;
import com.dgmoonlabs.cms.domain.business.order.constant.PassUnit;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Comment("판매하는 스트리밍 ID (스트리밍 설정에서 자동 관리, 상품 목록에는 나오지 않음)")
    private Long mediaId;

    @Comment("판매하는 유료 게시글 ID (게시글 설정에서 자동 관리, 상품 목록에는 나오지 않음)")
    private Long articleId;

    @Comment("카테고리 ID")
    private Long categoryId;

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

    @Comment("약정 기간(개월). 없으면 약정 없이 언제든 해지")
    private Integer commitmentMonths;

    @Comment("약정 중 해지 위약금 비율(%, 남은 회차 금액 기준)")
    private Integer earlyTerminationFeeRate;

    @Column(length = 200)
    @Comment("후원 추천 금액 (쉼표로 구분)")
    private String donationPresets;

    @Column(precision = 15, scale = 2)
    @Comment("후원·펀딩 목표 금액")
    private BigDecimal goalAmount;

    @Comment("펀딩 마감 일시")
    private LocalDateTime fundingDeadline;

    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("이용권 단위 (기간권·횟수권)")
    private PassUnit passUnit;

    @Comment("기간권 사용 기간(일)")
    private Integer passDays;

    @Comment("횟수권 횟수 (주문 수량만큼 곱해 발급)")
    private Integer passCount;

    @Comment("이 이용권으로 예약할 수 있는 예약 상품 ID")
    private Long passProgramId;

    @Comment("문의만 상품에서 상담 예약으로 연결할 예약 상품 ID")
    private Long inquiryProgramId;

    @Comment("선주문 판매 시작 일시 (없으면 지금부터)")
    private LocalDateTime preorderSaleStartAt;

    @Comment("선주문 판매 종료 일시 (없으면 계속 받음)")
    private LocalDateTime preorderSaleEndAt;

    @Comment("선주문 출고 예정일")
    private LocalDate preorderShipDate;

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

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("매장 주문 전용 상품 (포장·매장 식사로만 주문)")
    private boolean storeOrder;

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
        this.goalAmount = saleType == ProductSaleType.DONATION || saleType == ProductSaleType.FUNDING ? goalAmount : null;
    }

    public void changeFunding(final LocalDateTime deadline) {
        this.fundingDeadline = saleType == ProductSaleType.FUNDING ? deadline : null;
    }

    public void changePass(final PassUnit passUnit, final Integer passDays, final Integer passCount, final Long passProgramId) {
        boolean isPass = saleType == ProductSaleType.PASS;
        this.passUnit = isPass ? passUnit : null;
        this.passDays = isPass && passUnit == PassUnit.PERIOD ? passDays : null;
        this.passCount = isPass && passUnit == PassUnit.COUNT ? passCount : null;
        this.passProgramId = isPass ? passProgramId : null;
    }

    public void changeInquiry(final Long inquiryProgramId) {
        this.inquiryProgramId = saleType == ProductSaleType.INQUIRY ? inquiryProgramId : null;
    }

    public void changeCommitment(final Integer commitmentMonths, final Integer earlyTerminationFeeRate) {
        boolean committed = saleType == ProductSaleType.SUBSCRIPTION && commitmentMonths != null;
        this.commitmentMonths = committed ? commitmentMonths : null;
        this.earlyTerminationFeeRate = committed ? earlyTerminationFeeRate : null;
    }

    public void changePreorder(final LocalDateTime saleStartAt, final LocalDateTime saleEndAt, final LocalDate shipDate) {
        boolean isPreorder = saleType == ProductSaleType.PREORDER;
        this.preorderSaleStartAt = isPreorder ? saleStartAt : null;
        this.preorderSaleEndAt = isPreorder ? saleEndAt : null;
        this.preorderShipDate = isPreorder ? shipDate : null;
    }

    public record OptionGroup(String name, List<String> values) {
    }

    public void changeOptionGroups(final List<OptionGroup> optionGroups) {
        this.optionGroups = optionGroups == null || optionGroups.isEmpty() ? null : optionGroups;
    }

    public boolean hasOptionGroups() {
        return optionGroups != null && !optionGroups.isEmpty();
    }

    public void changeCategory(final Long categoryId) {
        this.categoryId = categoryId;
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

    public void changeStoreOrder(final boolean storeOrder) {
        this.storeOrder = storeOrder;
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
        if (!isEnabled || getDeletedAt() != null) {
            return false;
        }
        if (saleType == ProductSaleType.PREORDER) {
            LocalDateTime now = LocalDateTime.now();
            return (preorderSaleStartAt == null || !now.isBefore(preorderSaleStartAt))
                    && (preorderSaleEndAt == null || !now.isAfter(preorderSaleEndAt));
        }
        if (saleType == ProductSaleType.FUNDING) {
            return fundingDeadline == null || LocalDateTime.now().isBefore(fundingDeadline);
        }
        return true;
    }
}

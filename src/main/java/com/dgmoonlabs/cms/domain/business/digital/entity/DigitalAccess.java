package com.dgmoonlabs.cms.domain.business.digital.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(indexes = @Index(name = "idx_digital_access_member", columnList = "memberId"),
        uniqueConstraints = @UniqueConstraint(name = "uk_digital_access_order_product", columnNames = {"orderId", "productId"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class DigitalAccess extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false)
    @Comment("주문 ID")
    private Long orderId;

    @Column(nullable = false)
    @Comment("상품 ID")
    private Long productId;

    @Comment("결제 당시 상품명")
    private String productName;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("결제 당시 디지털 파일 ID 목록")
    private List<Long> fileIds;

    @Comment("내려받기 횟수 제한 (없으면 무제한)")
    private Integer downloadLimit;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("내려받은 횟수")
    private int downloadCount = 0;

    @Comment("내려받기 마감 일시 (없으면 무기한)")
    private LocalDateTime expiresAt;

    @Comment("주문 취소로 회수한 일시")
    private LocalDateTime revokedAt;

    public void useDownload(final Long fileId, final LocalDateTime now) {
        if (!fileIds.contains(fileId)) {
            throw BusinessException.notFound();
        }
        if (revokedAt != null) {
            throw new BusinessException(ErrorCode.DOWNLOAD_REVOKED);
        }
        if (expiresAt != null && now.isAfter(expiresAt)) {
            throw new BusinessException(ErrorCode.DOWNLOAD_EXPIRED);
        }
        if (downloadLimit != null && downloadCount >= downloadLimit) {
            throw new BusinessException(ErrorCode.DOWNLOAD_LIMIT_EXCEEDED);
        }
        downloadCount++;
    }

    public void revoke(final LocalDateTime now) {
        if (revokedAt == null) {
            revokedAt = now;
        }
    }

    public Integer remainingDownloads() {
        return downloadLimit == null ? null : Math.max(downloadLimit - downloadCount, 0);
    }

    public boolean isUsableAt(final LocalDateTime now) {
        return revokedAt == null && (expiresAt == null || !now.isAfter(expiresAt))
                && (downloadLimit == null || downloadCount < downloadLimit);
    }
}

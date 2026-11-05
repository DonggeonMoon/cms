package com.dgmoonlabs.cms.domain.business.license.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_license_key_product", columnList = "productId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class LicenseKey extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("상품 ID")
    private Long productId;

    @Column(nullable = false, unique = true)
    @Comment("키 코드")
    private String code;

    @Comment("배정된 주문 ID")
    private Long orderId;

    @Comment("배정된 회원 ID")
    private Long memberId;

    @Comment("배정 일시")
    private LocalDateTime assignedAt;

    public boolean isAssigned() {
        return orderId != null;
    }

    public void assign(final Long orderId, final Long memberId) {
        this.orderId = orderId;
        this.memberId = memberId;
        this.assignedAt = LocalDateTime.now();
    }
}

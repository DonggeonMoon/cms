package com.dgmoonlabs.cms.domain.streaming.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_streaming_access_member_streaming", columnList = "memberId, streamingId"),
        uniqueConstraints = @UniqueConstraint(name = "uk_streaming_access_order_streaming", columnNames = {"orderId", "streamingId"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class StreamingAccess extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false)
    @Comment("스트리밍 ID")
    private Long streamingId;

    @Column(nullable = false)
    @Comment("구매 주문 ID")
    private Long orderId;

    @Comment("시청 마감 일시 (없으면 무기한)")
    private LocalDateTime expiresAt;

    @Comment("주문 취소로 회수한 일시")
    private LocalDateTime revokedAt;

    public void revoke(final LocalDateTime now) {
        if (revokedAt == null) {
            revokedAt = now;
        }
    }
}

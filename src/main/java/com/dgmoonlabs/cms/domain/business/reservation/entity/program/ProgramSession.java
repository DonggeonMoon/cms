package com.dgmoonlabs.cms.domain.business.reservation.entity.program;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/** 예약 상품 하나를 여러 번 여는 회차. 프로그램은 수업(반)별 정원, 공연은 회차별 좌석 판매에 쓴다. */
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ProgramSession extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("예약 상품 ID")
    private Long programId;

    @Column(length = 100)
    @Comment("회차 이름 (화요일 저녁반, 2회차 등)")
    private String name;

    @Column(nullable = false)
    @Comment("시작 일시")
    private LocalDateTime startAt;

    @Comment("종료 일시")
    private LocalDateTime endAt;

    @Comment("정원 (없으면 제한 없음, 공연은 배치표 좌석 수)")
    private Integer capacity;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("예약된 인원·좌석 수")
    private int reservedCount = 0;

    public void update(final String name, final LocalDateTime startAt, final LocalDateTime endAt, final Integer capacity) {
        if (capacity != null && capacity < reservedCount) {
            throw new BusinessException(ErrorCode.PROGRAM_FULL);
        }
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
        this.capacity = capacity;
    }

    /** 시작한 회차는 더 받지 않는다. */
    public boolean isOpenAt(final LocalDateTime now) {
        return getDeletedAt() == null && now.isBefore(startAt);
    }

    public Integer remaining() {
        return capacity == null ? null : Math.max(0, capacity - reservedCount);
    }

    public void reserve(final int count) {
        if (capacity != null && reservedCount + count > capacity) {
            throw new BusinessException(ErrorCode.PROGRAM_FULL);
        }
        reservedCount += count;
    }

    public void release(final int count) {
        reservedCount = Math.max(0, reservedCount - count);
    }

    public LocalDateTime endOrStart() {
        return endAt != null ? endAt : startAt.plusMinutes(1);
    }
}

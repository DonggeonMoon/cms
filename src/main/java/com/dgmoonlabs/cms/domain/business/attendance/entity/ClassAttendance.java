package com.dgmoonlabs.cms.domain.business.attendance.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

/** 구독(수강료)으로 등록된 학생의 하루치 출결. 구독 1건·날짜 1개에 한 건만 있다. */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"subscription_id", "attended_on"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ClassAttendance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("구독 ID")
    private Long subscriptionId;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false)
    @Comment("수강료 상품 ID")
    private Long productId;

    @Column(nullable = false)
    @Comment("출결 날짜")
    private LocalDate attendedOn;

    @Column(nullable = false)
    @Comment("출석 여부")
    private boolean present;

    @Column(length = 200)
    @Comment("비고")
    private String note;

    public void mark(final boolean present, final String note) {
        this.present = present;
        this.note = note;
    }
}

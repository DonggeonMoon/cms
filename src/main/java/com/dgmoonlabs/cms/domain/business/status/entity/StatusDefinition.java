package com.dgmoonlabs.cms.domain.business.status.entity;

import com.dgmoonlabs.cms.domain.business.status.constant.StatusDomain;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_status_definition", columnNames = {"status_domain", "code"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class StatusDefinition extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status_domain", nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("대상 (주문/예약)")
    private StatusDomain domain;

    @Column(nullable = false, length = 40)
    @Comment("상태 코드 (기본 상태는 enum 이름, 세부 상태는 C_로 시작)")
    private String code;

    @Column(nullable = false, length = 50)
    @Comment("표시 이름")
    private String label;

    @Column(nullable = false, length = 40)
    @Comment("세부 상태가 속한 기본 상태 (기본 상태는 자기 자신)")
    private String baseStatus;

    @Column(name = "is_system", nullable = false)
    @ColumnDefault("false")
    @Comment("기본 상태 여부")
    private boolean system;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("표시 순서")
    private int sequence;

    public void rename(final String label) {
        this.label = label;
    }

    public void change(final String label, final String baseStatus, final int sequence) {
        this.label = label;
        this.baseStatus = baseStatus;
        this.sequence = sequence;
    }
}

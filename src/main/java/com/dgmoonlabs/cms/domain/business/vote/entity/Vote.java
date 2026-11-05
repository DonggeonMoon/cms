package com.dgmoonlabs.cms.domain.business.vote.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Vote extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    @Comment("안건 제목")
    private String title;

    @Column(length = 2000)
    @Comment("안건 설명")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("선택지 목록 (JSON 문자열 배열)")
    private String optionsJson;

    @Comment("투표 가능한 최소 회원 등급 (없으면 전체 회원)")
    private Long accessGradeId;

    @Comment("마감 일시 (null이면 진행 중)")
    private LocalDateTime closedAt;

    public boolean isOpen() {
        return closedAt == null;
    }

    public void close() {
        this.closedAt = LocalDateTime.now();
    }
}

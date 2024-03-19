package com.dgmoonlabs.cms.domain.business.reservation.entity.program;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Program extends BaseEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    @Comment("이름")
    private String name;

    @Comment("설명")
    private String description;

    @Comment("이미지 ID")
    private Long imageFileId;

    @Comment("가격")
    private BigDecimal price;

    @Comment("최대 정원")
    private Integer maximumPerson;

    @Comment("예약/신청 가능 인원")
    private Integer availablePerson;

    @Comment("시작 일시")
    private LocalDateTime startDateTime;

    @Comment("종료 일시")
    private LocalDateTime endDateTime;

    @Comment("신청 시작 일시")
    private LocalDateTime applicationStartDateTime;

    @Comment("신청 종료 일시")
    private LocalDateTime applicationEndDateTime;

    @Lob
    @Column(nullable = false)
    @ColumnDefault("''")
    @Comment("내용 텍스트")
    private String contentText;

    @Lob
    @Column(nullable = false)
    @ColumnDefault("''")
    @Comment("내용 HTML")
    private String contentHtml;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("추가 필드")
    private String fields;

    @Column(nullable = false)
    @ColumnDefault("1")
    @Comment("예약 가능 여부")
    private boolean isEnabled;
}

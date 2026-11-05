package com.dgmoonlabs.cms.domain.business.record.entity;

import com.dgmoonlabs.cms.global.crypto.EncryptedStringConverter;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

/** 회원별 시술·상담 이력. 내용은 건강·상담 정보일 수 있어 암호화해 둔다. */
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CustomerRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Comment("관련 예약 ID")
    private Long reservationId;

    @Column(nullable = false)
    @Comment("기록한 날 (시술·상담한 날)")
    private LocalDate recordedOn;

    @Column(length = 50)
    @Comment("분류 (시술, 상담, 진단 등)")
    private String category;

    @Column(nullable = false, length = 200)
    @Comment("제목")
    private String title;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "text")
    @Comment("내용")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("사진 파일 ID 목록")
    private List<Long> photoFileIds;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("회원에게 보여주기")
    private boolean sharedWithMember;

    @Comment("작성한 관리자 ID")
    private Long authorAdminId;

    @Column(length = 100)
    @Comment("작성한 관리자 이름")
    private String authorName;

    public void update(final Long reservationId, final LocalDate recordedOn, final String category, final String title,
                       final String content, final List<Long> photoFileIds, final boolean sharedWithMember) {
        this.reservationId = reservationId;
        this.recordedOn = recordedOn;
        this.category = category;
        this.title = title;
        this.content = content;
        this.photoFileIds = photoFileIds;
        this.sharedWithMember = sharedWithMember;
    }
}

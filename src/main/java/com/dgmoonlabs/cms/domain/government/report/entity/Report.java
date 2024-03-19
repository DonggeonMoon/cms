package com.dgmoonlabs.cms.domain.government.report.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Report extends BaseEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    @ColumnDefault("'제목 없음'")
    @Comment("제목")
    private String title;

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

    @Comment("추가 필드")
    private String fields;

    @Comment("사용자 고유식별번호")
    private String uid;

    @Comment("사용자 이메일")
    private String email;

    @Comment("사용자 전화번호")
    private String phoneNumber;
}

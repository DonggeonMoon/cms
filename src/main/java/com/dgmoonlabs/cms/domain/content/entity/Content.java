package com.dgmoonlabs.cms.domain.content.entity;

import com.dgmoonlabs.cms.domain.content.constant.ContentStatus;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Content extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("컨텐츠 상태")
    private ContentStatus status;

    @Column(nullable = false)
    @ColumnDefault("1")
    @Comment("컨텐츠 버전")
    private int version;

    @Comment("마지막 작업자")
    private String lastWorker;

    @Column(nullable = false)
    @ColumnDefault("''")
    @Comment("메모")
    private String memo;
}

package com.dgmoonlabs.cms.domain.course.entity;

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
public class Course extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    @Comment("과정 이름")
    private String title;

    @Column(length = 1000)
    @Comment("과정 소개")
    private String description;

    @Comment("썸네일 이미지 파일 ID")
    private Long thumbnailFileId;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("차시를 순서대로만 들을 수 있는지")
    private boolean sequential;

    @Comment("수강 가능 등급 ID (없으면 누구나)")
    private Long accessGradeId;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("공개 여부")
    private boolean isUsing;

    public void update(final String title, final String description, final Long thumbnailFileId, final boolean sequential,
                       final Long accessGradeId, final boolean isUsing) {
        this.title = title;
        this.description = description;
        this.thumbnailFileId = thumbnailFileId;
        this.sequential = sequential;
        this.accessGradeId = accessGradeId;
        this.isUsing = isUsing;
    }

    public boolean isPublic() {
        return isUsing && getDeletedAt() == null;
    }
}

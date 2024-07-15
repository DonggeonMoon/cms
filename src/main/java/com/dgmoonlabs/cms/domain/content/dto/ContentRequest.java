package com.dgmoonlabs.cms.domain.content.dto;

import com.dgmoonlabs.cms.domain.content.constant.ContentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ContentRequest {
    private Long id;

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

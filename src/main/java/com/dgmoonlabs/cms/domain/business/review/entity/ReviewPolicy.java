package com.dgmoonlabs.cms.domain.business.review.entity;

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
public class ReviewPolicy extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("리뷰 내용 최소 글자 수 (공백 제외, 0이면 제한 없음)")
    private int minimumContentLength = 0;

    public void update(final int minimumContentLength) {
        this.minimumContentLength = minimumContentLength;
    }
}

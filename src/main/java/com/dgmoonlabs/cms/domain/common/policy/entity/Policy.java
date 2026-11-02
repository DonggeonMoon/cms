package com.dgmoonlabs.cms.domain.common.policy.entity;

import com.dgmoonlabs.cms.domain.common.policy.constant.PolicyType;
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
public class Policy extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    @Comment("문서 종류")
    private PolicyType type;

    @Column(nullable = false, columnDefinition = "text")
    @ColumnDefault("''")
    @Comment("내용 HTML")
    private String contentHtml;

    @Column(nullable = false)
    @ColumnDefault("1")
    @Comment("버전")
    private int version;

    @Comment("마지막 수정자")
    private String lastWorker;

    public void update(final String contentHtml, final String worker) {
        this.contentHtml = contentHtml;
        this.lastWorker = worker;
        this.version++;
    }
}

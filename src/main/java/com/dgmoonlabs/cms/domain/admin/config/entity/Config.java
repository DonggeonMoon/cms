package com.dgmoonlabs.cms.domain.admin.config.entity;

import com.dgmoonlabs.cms.domain.admin.config.constant.OptionType;
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
public class Config extends BaseEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    @Comment("옵션 키")
    private String key;

    @Column(nullable = false)
    @ColumnDefault("''")
    @Comment("옵션 값")
    private String value;

    @Column(nullable = false)
    @ColumnDefault("''")
    @Comment("옵션 설명")
    private String description;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("옵션 타입")
    private OptionType type;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("숨김 여부")
    private boolean isHidden;
}

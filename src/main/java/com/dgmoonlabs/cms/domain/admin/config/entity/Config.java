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
    private String optionKey;

    @Column(nullable = false)
    @ColumnDefault("''")
    @Comment("옵션 값")
    private String optionValue;

    @Column(nullable = false)
    @ColumnDefault("''")
    private String description;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private OptionType type;

    @Column(nullable = false)
    private boolean isHidden;
}

package com.dgmoonlabs.cms.domain.admin.config.entity;

import com.dgmoonlabs.cms.domain.admin.config.constant.OptionType;
import com.dgmoonlabs.cms.domain.admin.config.dto.ConfigSaveRequest;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // key/value are reserved words in H2 (used by the tests), so the column names are quoted
    @Column(name = "`key`", nullable = false)
    @Comment("옵션 키")
    private String key;

    @Column(name = "`value`", nullable = false)
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
    @ColumnDefault("false")
    @Comment("숨김 여부")
    private boolean isHidden;

    public void update(final ConfigSaveRequest request) {
        this.value = request.valueOrEmpty();
        this.description = request.descriptionOrEmpty();
        this.type = request.type();
        this.isHidden = request.isHidden();
    }
}

package com.dgmoonlabs.cms.domain.admin.account.entity;

import com.dgmoonlabs.cms.domain.admin.account.constant.AdminArea;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class AdminGrade extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    @Comment("등급 이름")
    private String name;

    @Column(length = 300)
    @Comment("설명")
    private String description;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("최고 관리자 등급 (시스템 등급, 모든 영역과 관리자 관리)")
    private boolean superAdmin;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("접근할 수 있는 관리 영역")
    private List<AdminArea> areas;

    public void update(final String name, final String description, final List<AdminArea> areas) {
        this.name = name;
        this.description = description;
        if (!superAdmin) {
            this.areas = List.copyOf(areas);
        }
    }

    public boolean allows(final AdminArea area) {
        return superAdmin || (areas != null && areas.contains(area));
    }
}

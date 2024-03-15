package com.dgmoonlabs.cms.domain.common.user.entity.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class AdminOrganization {
    @Id
    private Long id;

    @Column(nullable = false)
    @Comment("관리자 ID")
    private Long memberId;

    @Comment("직장명")
    private String officeName;

    @Comment("부서명")
    private String officeDepartment;

    @Comment("직위")
    private String officePosition;

    @Comment("직장 주소")
    private String officeAddress;

    @Comment("직장 상세주소")
    private String officeDetailAddress;
}

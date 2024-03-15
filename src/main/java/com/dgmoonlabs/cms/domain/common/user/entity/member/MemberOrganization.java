package com.dgmoonlabs.cms.domain.common.user.entity.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MemberOrganization {
    @Id
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
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

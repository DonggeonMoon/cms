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
public class AdminAddress {
    @Id
    private Long id;

    @Column(nullable = false)
    @Comment("관리자 ID")
    private Long adminId;

    @Comment("우편번호")
    private String postalCode;

    @Comment("주소")
    private String address;

    @Comment("상세주소")
    private String detailAddress;
}

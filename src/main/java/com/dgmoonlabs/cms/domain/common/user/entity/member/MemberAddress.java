package com.dgmoonlabs.cms.domain.common.user.entity.member;

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
public class MemberAddress {
    @Id
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Comment("우편번호")
    private String postalCode;

    @Comment("주소")
    private String address;

    @Comment("상세주소")
    private String detailAddress;
}

package com.dgmoonlabs.cms.domain.common.user.entity.guest.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Guest extends BaseEntity {
    @Id
    private Long id;

    @Comment("이름")
    private String name;

    @Comment("IP 주소")
    private String ipAddress;

    @Comment("생년월일")
    private LocalDate birthDate;

    @Comment("성별")
    private String gender;

    @Comment("이메일")
    private String email;

    @Comment("전화번호")
    private String telephoneName;

    @Comment("휴대폰 번호")
    private String mobilePhoneName;
}

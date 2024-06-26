package com.dgmoonlabs.cms.domain.common.user.entity.member;

import com.dgmoonlabs.cms.domain.common.user.dto.MemberModifyRequest;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private String username;

    @Column(nullable = false)
    @Comment("비밀번호")
    private String password;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("로그인 시도 횟수")
    private int loginTryCount;

    @Comment("마지막 로그인 일시")
    private LocalDateTime lastLoginDateTime;

    @Comment("권한")
    private Long roleId;

    @Comment("그룹")
    private Long groupId;

    @Comment("약관 동의 확인 일시")
    private LocalDate termConfirmationDate;

    @Comment("약관 동의 만료 일시")
    private LocalDate termExpirationDate;

    @Comment("이름")
    private String name;

    @Comment("성")
    private String lastName;

    @Comment("이름만")
    private String firstName;

    @Comment("생년월일")
    private LocalDate birthDate;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("음력 여부")
    private boolean isLunarBirthDate;

    @Comment("성별")
    private String gender;

    @Comment("이메일")
    private String email;

    @Comment("전화번호")
    private String telephoneNumber;

    @Comment("휴대폰 번호")
    private String mobilePhoneNumber;

    public void update(MemberModifyRequest request) {
        this.username = request.getUsername();
        this.password = request.getPassword();
        this.name = request.getName();
        this.lastName = request.getLastName();
        this.firstName = request.getFirstName();
        this.isLunarBirthDate = request.getIsLunarBirthDate();
        this.birthDate = request.getBirthDate();
        this.gender = request.getGender();
        this.email = request.getEmail();
        this.telephoneNumber = request.getTelephoneName();
        this.mobilePhoneNumber = request.getMobilePhoneNumber();
    }
}

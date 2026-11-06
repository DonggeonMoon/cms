package com.dgmoonlabs.cms.domain.common.user.entity.member;

import com.dgmoonlabs.cms.global.crypto.EncryptedStringConverter;
import com.dgmoonlabs.cms.domain.common.user.constant.PersonalInfoItem;
import com.dgmoonlabs.cms.domain.common.user.dto.MemberModifyRequest;
import com.dgmoonlabs.cms.domain.common.user.dto.MemberName;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Comment("닉네임")
    @Column(length = 30, unique = true)
    private String nickname;

    @Comment("이름")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String name;

    @Comment("성")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String lastName;

    @Comment("이름만")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String firstName;

    @Comment("생년월일")
    private LocalDate birthDate;

    @Comment("성별")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String gender;

    @Comment("이메일")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String email;

    @Comment("전화번호")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String telephoneNumber;

    @Comment("휴대폰 번호")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String mobilePhoneNumber;

    @Comment("휴대폰 번호 국가 코드 (예: +82)")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String mobilePhoneCountryCode;

    @Comment("회원 등급 ID (없으면 기본 등급)")
    private Long gradeId;

    @Comment("회원 등급 만료 일시 (없으면 무기한)")
    private LocalDateTime gradeExpiresAt;

    @Comment("등급 만료 후 돌아갈 등급 ID (없으면 기본 등급)")
    private Long baseGradeId;

    @Comment("추천인(가입 시 입력한 추천인 회원) ID")
    private Long referredByMemberId;

    @Column(nullable = false)
    @ColumnDefault("false")
    @Comment("추천인 보상 지급 여부 (피추천인 첫 결제 때 한 번만 지급)")
    private boolean referralRewardGranted;

    public void markReferralRewardGranted() {
        this.referralRewardGranted = true;
    }

    public String displayName() {
        return Stream.of(nickname, username, name)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    public void updateProfile(final MemberModifyRequest request, final MemberName memberName, final String nickname,
                              final MemberJoinSetting setting) {
        this.nickname = nickname;
        this.name = memberName.name();
        this.lastName = memberName.lastName();
        this.firstName = memberName.firstName();
        this.birthDate = setting.keep(PersonalInfoItem.BIRTH_DATE, request.birthDate());
        this.gender = setting.keep(PersonalInfoItem.GENDER, request.gender());
        this.email = setting.keep(PersonalInfoItem.EMAIL, request.email());
        this.telephoneNumber = setting.keep(PersonalInfoItem.TELEPHONE, request.telephoneNumber());
        this.mobilePhoneNumber = setting.keep(PersonalInfoItem.MOBILE_PHONE, request.mobilePhoneNumber());
        this.mobilePhoneCountryCode = setting.keepMobilePhoneCountryCode(request.mobilePhoneCountryCode());
    }

    public void changePassword(final String encodedPassword) {
        this.password = encodedPassword;
    }

    public boolean isLocked(final int maxLoginTryCount) {
        return loginTryCount >= maxLoginTryCount;
    }

    public void recordLoginSuccess() {
        this.loginTryCount = 0;
        this.lastLoginDateTime = LocalDateTime.now();
    }

    public void recordLoginFailure() {
        this.loginTryCount++;
    }

    public void unlock() {
        this.loginTryCount = 0;
    }

    public boolean isGradeExpired(final LocalDateTime now) {
        return gradeExpiresAt != null && !gradeExpiresAt.isAfter(now);
    }

    public Long currentGradeId(final LocalDateTime now) {
        return isGradeExpired(now) ? baseGradeId : gradeId;
    }

    public void changeGrade(final Long gradeId, final LocalDateTime expiresAt, final Long baseGradeId) {
        this.gradeId = gradeId;
        this.gradeExpiresAt = expiresAt;
        this.baseGradeId = expiresAt == null ? null : baseGradeId;
    }
}

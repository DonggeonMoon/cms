package com.dgmoonlabs.cms.domain.common.user.entity.guest.entity;

import com.dgmoonlabs.cms.global.crypto.EncryptedStringConverter;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("이름")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String name;

    @Comment("IP 주소")
    private String ipAddress;

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
    private String telephoneName;

    @Comment("휴대폰 번호")
    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = EncryptedStringConverter.COLUMN_LENGTH)
    private String mobilePhoneName;
}

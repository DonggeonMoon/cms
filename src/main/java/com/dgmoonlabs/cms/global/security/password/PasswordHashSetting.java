package com.dgmoonlabs.cms.global.security.password;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PasswordHashSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("새로 저장하는 비밀번호의 해시 알고리즘")
    private PasswordHashAlgorithm algorithm = PasswordHashAlgorithm.BCRYPT;

    public void change(final PasswordHashAlgorithm algorithm) {
        this.algorithm = algorithm;
    }
}

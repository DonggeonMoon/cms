package com.dgmoonlabs.cms.domain.common.regex.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class RegexRule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @Comment("내부 식별 코드")
    private String ruleKey;

    @Column(nullable = false, length = 100)
    @Comment("이름")
    private String name;

    @Column(nullable = false, length = 500)
    @Comment("정규식")
    private String pattern;

    @Column(nullable = false, length = 200)
    @Comment("맞지 않을 때 보여 줄 메시지")
    private String message;

    @Column(length = 100)
    @Comment("입력 안내 (입력란 placeholder)")
    private String hint;

    @Column(length = 500)
    @Comment("설명")
    private String description;

    public void update(final String name, final String pattern, final String message, final String hint,
                       final String description) {
        this.name = name;
        this.pattern = pattern;
        this.message = message;
        this.hint = hint;
        this.description = description;
    }
}

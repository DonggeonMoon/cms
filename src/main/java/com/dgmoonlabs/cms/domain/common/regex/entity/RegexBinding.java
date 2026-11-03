package com.dgmoonlabs.cms.domain.common.regex.entity;

import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class RegexBinding extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    @Comment("검사 대상")
    private RegexTarget target;

    @Comment("사용할 정규식 ID (없으면 검사하지 않음)")
    private Long regexRuleId;

    public void select(final Long regexRuleId) {
        this.regexRuleId = regexRuleId;
    }
}

package com.dgmoonlabs.cms.domain.common.i18n.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_translation", columnNames = {"language", "messageKey"}))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Translation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    @Comment("언어 코드")
    private String language;

    @Column(nullable = false, length = 200)
    @Comment("번역 키")
    private String messageKey;

    @Column(nullable = false, columnDefinition = "text")
    @Comment("번역문")
    private String message;
}

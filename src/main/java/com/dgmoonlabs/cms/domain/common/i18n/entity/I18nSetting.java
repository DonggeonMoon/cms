package com.dgmoonlabs.cms.domain.common.i18n.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.Arrays;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class I18nSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    @Comment("사용하는 언어 코드 (쉼표로 구분)")
    private String languages;

    @Column(nullable = false, length = 10)
    @Comment("기본 언어 코드")
    private String defaultLanguage;

    public static I18nSetting defaults() {
        return I18nSetting.builder().languages("ko").defaultLanguage("ko").build();
    }

    public List<String> languageList() {
        return Arrays.stream(languages.split(",")).map(String::trim).filter(code -> !code.isEmpty()).toList();
    }

    public void update(final List<String> languages, final String defaultLanguage) {
        this.languages = String.join(",", languages);
        this.defaultLanguage = defaultLanguage;
    }
}

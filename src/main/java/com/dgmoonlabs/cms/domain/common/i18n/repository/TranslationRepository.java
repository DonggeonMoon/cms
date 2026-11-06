package com.dgmoonlabs.cms.domain.common.i18n.repository;

import com.dgmoonlabs.cms.domain.common.i18n.entity.Translation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranslationRepository extends JpaRepository<Translation, Long> {
    List<Translation> findAllByLanguageOrderByMessageKeyAsc(String language);

    boolean existsByLanguageAndMessageKey(String language, String messageKey);
}

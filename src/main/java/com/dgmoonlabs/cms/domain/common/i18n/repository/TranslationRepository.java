package com.dgmoonlabs.cms.domain.common.i18n.repository;

import com.dgmoonlabs.cms.domain.common.i18n.entity.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TranslationRepository extends JpaRepository<Translation, Long> {
    List<Translation> findAllByLanguageOrderByMessageKeyAsc(String language);

    @Modifying
    @Query("delete from Translation t where t.language = :language")
    void deleteAllByLanguage(String language);
}

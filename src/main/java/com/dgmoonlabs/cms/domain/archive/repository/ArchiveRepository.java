package com.dgmoonlabs.cms.domain.archive.repository;

import com.dgmoonlabs.cms.domain.archive.entity.article.Archive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveRepository extends JpaRepository<Archive, Long>, ArchiveCustomRepository {
}

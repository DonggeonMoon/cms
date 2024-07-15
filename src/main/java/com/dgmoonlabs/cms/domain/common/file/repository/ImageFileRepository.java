package com.dgmoonlabs.cms.domain.common.file.repository;

import com.dgmoonlabs.cms.domain.common.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageFileRepository extends JpaRepository<File, Long>, FileCustomRepository {
}

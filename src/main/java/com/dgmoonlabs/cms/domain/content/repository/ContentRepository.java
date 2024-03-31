package com.dgmoonlabs.cms.domain.content.repository;

import com.dgmoonlabs.cms.domain.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ContentRepository extends JpaRepository<Content, Long>, ContentCustomRepository {
}
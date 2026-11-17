package com.dgmoonlabs.cms.domain.postcode.repository;

import com.dgmoonlabs.cms.domain.postcode.entity.PostcodeSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostcodeSettingRepository extends JpaRepository<PostcodeSetting, Long> {
    Optional<PostcodeSetting> findFirstByOrderByIdAsc();
}

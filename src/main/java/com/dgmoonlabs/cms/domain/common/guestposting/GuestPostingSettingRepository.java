package com.dgmoonlabs.cms.domain.common.guestposting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestPostingSettingRepository extends JpaRepository<GuestPostingSetting, Long> {
    Optional<GuestPostingSetting> findFirstByOrderByIdAsc();
}

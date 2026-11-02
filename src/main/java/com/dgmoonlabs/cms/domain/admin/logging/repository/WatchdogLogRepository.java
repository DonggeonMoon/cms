package com.dgmoonlabs.cms.domain.admin.logging.repository;

import com.dgmoonlabs.cms.domain.admin.logging.entity.WatchdogLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchdogLogRepository extends JpaRepository<WatchdogLog, Long> {
    Page<WatchdogLog> findAllByMemberUsernameContainingIgnoreCaseOrUriContainingIgnoreCaseOrderByIdDesc(
            String username, String uri, Pageable pageable);
}

package com.dgmoonlabs.cms.domain.admin.logging.repository;

import com.dgmoonlabs.cms.domain.admin.logging.dto.WatchdogLogRequest;
import com.dgmoonlabs.cms.domain.admin.logging.entity.WatchdogLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WatchdogCustomRepository {
    Page<WatchdogLog> find(WatchdogLogRequest request, Pageable pageable);

    List<WatchdogLog> find(WatchdogLogRequest request);
}

package com.dgmoonlabs.cms.domain.admin.ipaccess.repository;

import com.dgmoonlabs.cms.domain.admin.ipaccess.entity.AllowedIp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllowedIpRepository extends JpaRepository<AllowedIp, Long> {
    List<AllowedIp> findAllByOrderByIdAsc();

    boolean existsByAddress(String address);

    boolean existsByAddressAndIdNot(String address, Long id);
}

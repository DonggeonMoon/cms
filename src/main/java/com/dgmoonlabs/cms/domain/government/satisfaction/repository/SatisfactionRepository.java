package com.dgmoonlabs.cms.domain.government.satisfaction.repository;

import com.dgmoonlabs.cms.domain.government.satisfaction.entity.Satisfaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SatisfactionRepository extends JpaRepository<Satisfaction, Long> {
    Optional<Satisfaction> findByMenuId(Long menuId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Satisfaction s where s.menuId = :menuId")
    Optional<Satisfaction> findForUpdate(Long menuId);

    List<Satisfaction> findAllByOrderByMenuIdAsc();
}

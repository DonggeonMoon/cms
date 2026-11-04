package com.dgmoonlabs.cms.domain.common.user.repository;

import com.dgmoonlabs.cms.domain.common.user.entity.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsernameAndDeletedAtIsNull(String username);

    boolean existsByUsername(String username);

    List<Admin> findAllByDeletedAtIsNullOrderByIdAsc();

    @Query("select a.id from Admin a where a.deletedAt is null order by a.id")
    List<Long> findActiveIds();

    boolean existsByGradeIdAndDeletedAtIsNull(Long gradeId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Admin a set a.gradeId = :gradeId where a.id = :id")
    int assignGrade(Long id, Long gradeId);

    long countByGradeIdAndDeletedAtIsNull(Long gradeId);
}

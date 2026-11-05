package com.dgmoonlabs.cms.domain.business.record.repository;

import com.dgmoonlabs.cms.domain.business.record.entity.CustomerRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CustomerRecordRepository extends JpaRepository<CustomerRecord, Long> {
    Optional<CustomerRecord> findByIdAndDeletedAtIsNull(Long id);

    /** 내용은 암호화되어 있어 검색하지 않는다. 제목·분류로만 찾는다. */
    @Query("select r from CustomerRecord r where r.deletedAt is null and (:memberId is null or r.memberId = :memberId) "
            + "and (:reservationId is null or r.reservationId = :reservationId) "
            + "and (:keyword = '' or lower(r.title) like lower(concat('%', :keyword, '%')) "
            + "     or lower(r.category) like lower(concat('%', :keyword, '%'))) "
            + "order by r.recordedOn desc, r.id desc")
    Page<CustomerRecord> search(Long memberId, Long reservationId, String keyword, Pageable pageable);

    Page<CustomerRecord> findAllByMemberIdAndSharedWithMemberTrueAndDeletedAtIsNullOrderByRecordedOnDescIdDesc(Long memberId,
                                                                                                          Pageable pageable);
}

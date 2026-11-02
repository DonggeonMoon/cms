package com.dgmoonlabs.cms.domain.business.review.repository;

import com.dgmoonlabs.cms.domain.business.review.constant.ReviewTargetType;
import com.dgmoonlabs.cms.domain.business.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByIdAndDeletedAtIsNull(Long id);

    Page<Review> findAllByProductIdAndDeletedAtIsNullOrderByIdDesc(Long productId, Pageable pageable);

    Page<Review> findAllByProgramIdAndDeletedAtIsNullOrderByIdDesc(Long programId, Pageable pageable);

    Page<Review> findAllByTargetTypeAndDeletedAtIsNullOrderByIdDesc(ReviewTargetType targetType, Pageable pageable);

    boolean existsByProductIdAndUserIdAndDeletedAtIsNull(Long productId, Long userId);

    boolean existsByProgramIdAndUserIdAndDeletedAtIsNull(Long programId, Long userId);

    @Query("select avg(r.rating) from Review r where r.productId = :productId and r.deletedAt is null")
    Double averageRating(Long productId);

    @Query("select avg(r.rating) from Review r where r.programId = :programId and r.deletedAt is null")
    Double averageProgramRating(Long programId);
}

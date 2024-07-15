package com.dgmoonlabs.cms.domain.business.review.repository;

import com.dgmoonlabs.cms.domain.business.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewCustomRepository {
}

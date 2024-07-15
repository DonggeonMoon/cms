package com.dgmoonlabs.cms.domain.business.review.repository;

import com.dgmoonlabs.cms.domain.business.review.dto.ReviewRequest;
import com.dgmoonlabs.cms.domain.business.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewCustomRepository {
    Page<Review> find(ReviewRequest reviewRequest, Pageable pageable);

    List<Review> find(ReviewRequest reviewRequest);
}

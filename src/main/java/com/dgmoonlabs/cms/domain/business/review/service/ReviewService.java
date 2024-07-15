package com.dgmoonlabs.cms.domain.business.review.service;

import com.dgmoonlabs.cms.domain.business.review.dto.ReviewRequest;
import com.dgmoonlabs.cms.domain.business.review.entity.Review;
import com.dgmoonlabs.cms.domain.business.review.repository.ReviewRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public Page<Review> getReview(ReviewRequest reviewRequest, Pageable pageable) {
        return reviewRepository.find(reviewRequest, pageable);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewWithoutPaging(ReviewRequest reviewRequest, Pageable pageable) {
        return reviewRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Review getReview(long id) {
        return reviewRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    @Transactional
    public void updateReview(HttpServletRequest request) {
    }
}

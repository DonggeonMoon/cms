package com.dgmoonlabs.cms.domain.business.review.repository.impl;

import com.dgmoonlabs.cms.domain.business.review.dto.ReviewRequest;
import com.dgmoonlabs.cms.domain.business.review.entity.Review;
import com.dgmoonlabs.cms.domain.business.review.repository.ReviewCustomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class ReviewCustomRepositoryImpl implements ReviewCustomRepository {
    @Override
    public Page<Review> find(final ReviewRequest reviewRequest, final Pageable pageable) {
        return null;
    }

    @Override
    public List<Review> find(final ReviewRequest reviewRequest) {
        return List.of();
    }
}

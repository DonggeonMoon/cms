package com.dgmoonlabs.cms.domain.business.review.dto;

import com.dgmoonlabs.cms.domain.business.review.constant.ReviewTargetType;
import com.dgmoonlabs.cms.domain.business.review.entity.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

public final class ReviewDtos {
    private ReviewDtos() {
    }

    @Builder
    public record ReviewSaveRequest(
            @Min(1) @Max(5) int rating,
            @NotBlank @Size(max = 200) String title,
            @Size(max = 20_000) String contentHtml
    ) {
    }

    public record ReviewResponse(Long id, ReviewTargetType targetType, Long productId, Long reservationItemId, int rating,
                                 String title, String contentHtml, String authorName, boolean canDelete,
                                 LocalDateTime createdAt) {
        public static ReviewResponse of(final Review review, final boolean canDelete) {
            return new ReviewResponse(review.getId(), review.getTargetType(), review.getProductId(), review.getReservationItemId(),
                    review.getRating(), review.getTitle(), review.getContentHtml(), review.getAuthorName(), canDelete,
                    review.getCreatedAt());
        }
    }
}

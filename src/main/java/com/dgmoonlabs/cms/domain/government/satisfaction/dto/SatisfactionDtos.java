package com.dgmoonlabs.cms.domain.government.satisfaction.dto;

import com.dgmoonlabs.cms.domain.government.satisfaction.entity.Satisfaction;
import com.dgmoonlabs.cms.domain.government.satisfaction.entity.SatisfactionVote;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class SatisfactionDtos {
    private SatisfactionDtos() {
    }

    public record VoteRequest(@NotNull Long menuId, @Min(1) @Max(5) int score, @Size(max = 500) String opinion) {
    }

    public record SatisfactionResponse(Long menuId, String menuName, int fivePoint, int fourPoint, int threePoint,
                                       int twoPoint, int onePoint, int total, double average, Integer myScore) {
        public static SatisfactionResponse of(final Long menuId, final String menuName, final Satisfaction tally,
                                              final Integer myScore) {
            Satisfaction value = tally == null ? Satisfaction.empty(menuId) : tally;
            return new SatisfactionResponse(menuId, menuName, value.getFivePoint(), value.getFourPoint(),
                    value.getThreePoint(), value.getTwoPoint(), value.getOnePoint(), value.total(),
                    Math.round(value.average() * 100) / 100.0, myScore);
        }
    }

    public record OpinionResponse(int score, String opinion, LocalDateTime createdAt) {
        public static OpinionResponse from(final SatisfactionVote vote) {
            return new OpinionResponse(vote.getScore(), vote.getOpinion(), vote.getCreatedAt());
        }
    }
}

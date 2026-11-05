package com.dgmoonlabs.cms.domain.business.vote.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class VoteDtos {
    private VoteDtos() {
    }

    public record VoteSaveRequest(@NotBlank String title, String description,
                                  @NotEmpty List<String> options, Long accessGradeId) {
    }

    public record BallotRequest(@NotNull Integer optionIndex) {
    }

    public record VoteResponse(Long id, String title, String description, List<String> options,
                               Long accessGradeId, boolean open, LocalDateTime closedAt,
                               int totalBallots, Map<Integer, Long> resultCounts, LocalDateTime createdAt) {
    }

    public record MyVoteResponse(Long id, String title, String description, List<String> options,
                                 boolean open, boolean eligible, Integer myOptionIndex,
                                 Integer totalBallots, Map<Integer, Long> resultCounts, LocalDateTime createdAt) {
    }
}

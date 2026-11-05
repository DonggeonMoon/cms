package com.dgmoonlabs.cms.domain.business.vote.service;

import com.dgmoonlabs.cms.domain.business.vote.dto.VoteDtos.BallotRequest;
import com.dgmoonlabs.cms.domain.business.vote.dto.VoteDtos.MyVoteResponse;
import com.dgmoonlabs.cms.domain.business.vote.dto.VoteDtos.VoteResponse;
import com.dgmoonlabs.cms.domain.business.vote.dto.VoteDtos.VoteSaveRequest;
import com.dgmoonlabs.cms.domain.business.vote.entity.Vote;
import com.dgmoonlabs.cms.domain.business.vote.entity.VoteBallot;
import com.dgmoonlabs.cms.domain.business.vote.repository.VoteBallotRepository;
import com.dgmoonlabs.cms.domain.business.vote.repository.VoteRepository;
import com.dgmoonlabs.cms.domain.common.grade.service.GradeService;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteService {
    private final VoteRepository voteRepository;
    private final VoteBallotRepository voteBallotRepository;
    private final GradeService gradeService;
    private final JsonMapper jsonMapper;

    @Transactional
    public VoteResponse create(final VoteSaveRequest request) {
        if (request.options().size() < 2) {
            throw new BusinessException(ErrorCode.VOTE_OPTIONS_REQUIRED);
        }
        Vote vote = Vote.builder()
                .title(request.title())
                .description(request.description())
                .optionsJson(jsonMapper.writeValueAsString(request.options()))
                .accessGradeId(gradeService.validGradeId(request.accessGradeId()))
                .build();
        return toResponse(voteRepository.save(vote));
    }

    @Transactional(readOnly = true)
    public Page<VoteResponse> search(final Pageable pageable) {
        return voteRepository.findAllByOrderByIdDesc(pageable).map(this::toResponse);
    }

    @Transactional
    public VoteResponse close(final Long id) {
        Vote vote = findVote(id);
        if (!vote.isOpen()) {
            throw new BusinessException(ErrorCode.VOTE_ALREADY_CLOSED);
        }
        vote.close();
        return toResponse(vote);
    }

    @Transactional
    public void remove(final Long id) {
        Vote vote = findVote(id);
        if (!voteBallotRepository.findAllByVoteId(id).isEmpty()) {
            throw new BusinessException(ErrorCode.VOTE_HAS_BALLOTS);
        }
        voteRepository.delete(vote);
    }

    @Transactional
    public MyVoteResponse castBallot(final Long memberId, final Long voteId, final BallotRequest request) {
        Vote vote = findVote(voteId);
        if (!vote.isOpen()) {
            throw new BusinessException(ErrorCode.VOTE_ALREADY_CLOSED);
        }
        gradeService.require(vote.getAccessGradeId());
        List<String> options = optionsOf(vote);
        if (request.optionIndex() < 0 || request.optionIndex() >= options.size()) {
            throw new BusinessException(ErrorCode.INVALID_VOTE_OPTION);
        }
        if (voteBallotRepository.findByVoteIdAndMemberId(voteId, memberId).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_VOTED);
        }
        voteBallotRepository.save(VoteBallot.builder().voteId(voteId).memberId(memberId)
                .optionIndex(request.optionIndex()).build());
        return toMyResponse(vote, memberId);
    }

    @Transactional(readOnly = true)
    public Page<MyVoteResponse> searchForMember(final Long memberId, final Pageable pageable) {
        return voteRepository.findAllByOrderByIdDesc(pageable).map(vote -> toMyResponse(vote, memberId));
    }

    private MyVoteResponse toMyResponse(final Vote vote, final Long memberId) {
        List<String> options = optionsOf(vote);
        boolean eligible = gradeService.meets(vote.getAccessGradeId());
        Integer myOptionIndex = voteBallotRepository.findByVoteIdAndMemberId(vote.getId(), memberId)
                .map(VoteBallot::getOptionIndex).orElse(null);
        boolean revealed = !vote.isOpen();
        List<VoteBallot> ballots = revealed ? voteBallotRepository.findAllByVoteId(vote.getId()) : List.of();
        return new MyVoteResponse(vote.getId(), vote.getTitle(), vote.getDescription(), options, vote.isOpen(),
                eligible, myOptionIndex, revealed ? ballots.size() : null,
                revealed ? tally(ballots) : null, vote.getCreatedAt());
    }

    private VoteResponse toResponse(final Vote vote) {
        List<VoteBallot> ballots = voteBallotRepository.findAllByVoteId(vote.getId());
        return new VoteResponse(vote.getId(), vote.getTitle(), vote.getDescription(), optionsOf(vote),
                vote.getAccessGradeId(), vote.isOpen(), vote.getClosedAt(), ballots.size(), tally(ballots), vote.getCreatedAt());
    }

    private Map<Integer, Long> tally(final List<VoteBallot> ballots) {
        return ballots.stream().collect(Collectors.groupingBy(VoteBallot::getOptionIndex, Collectors.counting()));
    }

    private List<String> optionsOf(final Vote vote) {
        return List.of(jsonMapper.readValue(vote.getOptionsJson(), String[].class));
    }

    private Vote findVote(final Long id) {
        return voteRepository.findById(id).orElseThrow(BusinessException::notFound);
    }
}

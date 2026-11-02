package com.dgmoonlabs.cms.domain.government.satisfaction.service;

import com.dgmoonlabs.cms.domain.admin.menu.entity.Menu;
import com.dgmoonlabs.cms.domain.admin.menu.repository.MenuRepository;
import com.dgmoonlabs.cms.domain.government.satisfaction.dto.SatisfactionDtos.OpinionResponse;
import com.dgmoonlabs.cms.domain.government.satisfaction.dto.SatisfactionDtos.SatisfactionResponse;
import com.dgmoonlabs.cms.domain.government.satisfaction.dto.SatisfactionDtos.VoteRequest;
import com.dgmoonlabs.cms.domain.government.satisfaction.entity.Satisfaction;
import com.dgmoonlabs.cms.domain.government.satisfaction.entity.SatisfactionVote;
import com.dgmoonlabs.cms.domain.government.satisfaction.repository.SatisfactionRepository;
import com.dgmoonlabs.cms.domain.government.satisfaction.repository.SatisfactionVoteRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SatisfactionService {
    private final SatisfactionRepository satisfactionRepository;
    private final SatisfactionVoteRepository voteRepository;
    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public SatisfactionResponse getSatisfaction(final Long menuId) {
        Menu menu = findMenu(menuId);
        Integer myScore = CurrentUser.get()
                .flatMap(user -> voteRepository.findByMenuIdAndMemberId(menuId, user.id()))
                .map(SatisfactionVote::getScore)
                .orElse(null);
        return SatisfactionResponse.of(menuId, menu.getName(), satisfactionRepository.findByMenuId(menuId).orElse(null), myScore);
    }

    @Transactional
    public SatisfactionResponse vote(final LoginUser member, final VoteRequest request) {
        Menu menu = findMenu(request.menuId());
        if (voteRepository.findByMenuIdAndMemberId(menu.getId(), member.id()).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_SUBMITTED);
        }
        Satisfaction tally = satisfactionRepository.findForUpdate(menu.getId())
                .orElseGet(() -> satisfactionRepository.save(Satisfaction.empty(menu.getId())));
        tally.add(request.score());
        voteRepository.save(SatisfactionVote.builder()
                .menuId(menu.getId())
                .memberId(member.id())
                .score(request.score())
                .opinion(request.opinion() == null || request.opinion().isBlank() ? null : request.opinion().trim())
                .build());
        return SatisfactionResponse.of(menu.getId(), menu.getName(), tally, request.score());
    }

    @Transactional(readOnly = true)
    public List<SatisfactionResponse> getAll() {
        Map<Long, Satisfaction> tallies = satisfactionRepository.findAllByOrderByMenuIdAsc().stream()
                .collect(Collectors.toMap(Satisfaction::getMenuId, Function.identity()));
        return menuRepository.findAllByDeletedAtIsNull().stream()
                .filter(menu -> tallies.containsKey(menu.getId()))
                .map(menu -> SatisfactionResponse.of(menu.getId(), menu.getName(), tallies.get(menu.getId()), null))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<OpinionResponse> getOpinions(final Long menuId, final Pageable pageable) {
        return voteRepository.findAllByMenuIdAndOpinionIsNotNullOrderByIdDesc(menuId, pageable).map(OpinionResponse::from);
    }

    @Transactional
    public void reset(final Long menuId) {
        voteRepository.deleteAllByMenuId(menuId);
        satisfactionRepository.findByMenuId(menuId).ifPresent(satisfactionRepository::delete);
    }

    private Menu findMenu(final Long menuId) {
        return menuRepository.findByIdAndDeletedAtIsNull(menuId).orElseThrow(BusinessException::notFound);
    }
}

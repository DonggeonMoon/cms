package com.dgmoonlabs.cms.domain.government.satisfaction.service;

import com.dgmoonlabs.cms.domain.government.satisfaction.dto.SatisfactionRequest;
import com.dgmoonlabs.cms.domain.government.satisfaction.entity.Satisfaction;
import com.dgmoonlabs.cms.domain.government.satisfaction.repository.SatisfactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SatisfactionService {
    private final SatisfactionRepository satisfactionRepository;

    @Transactional(readOnly = true)
    public Page<Satisfaction> getSatisfaction(SatisfactionRequest request, Pageable pageable) {
        return satisfactionRepository.find(request, pageable);
    }

    @Transactional(readOnly = true)
    public List<Satisfaction> getSatisfactionWithoutPaging(SatisfactionRequest request) {
        return satisfactionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Satisfaction getSatisfaction(Long menuId, Long userId) {
        return satisfactionRepository.findByMenuIdAndUserId(menuId, userId).orElseThrow(RuntimeException::new);
    }

    @Transactional
    public Long saveSatisfaction(SatisfactionRequest satisfactionRequest) {
        return satisfactionRepository.save(satisfactionRequest.toEntity()).getId();
    }

    @Transactional
    public void updateSatisfaction(SatisfactionRequest request) {
        Satisfaction satisfaction = satisfactionRepository.findByMenuIdAndUserId(request.getMenuId(), request.getId())
                .orElseThrow(RuntimeException::new);
        satisfaction.update(request.getFivePoint(), request.getFourPoint(), request.getThreePoint(), request.getTwoPoint(), request.getOnePoint());
    }

    @Transactional
    public void deleteSatisfaction(Long id) {
        satisfactionRepository.deleteById(id);
    }
}

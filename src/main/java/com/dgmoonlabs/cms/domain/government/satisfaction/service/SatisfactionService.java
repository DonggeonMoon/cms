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
    public Satisfaction getSatisfaction(long id) {
        return satisfactionRepository.findById(id).orElseThrow(RuntimeException::new);
    }
}

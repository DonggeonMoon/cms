package com.dgmoonlabs.cms.domain.government.satisfaction.repository;

import com.dgmoonlabs.cms.domain.government.satisfaction.dto.SatisfactionRequest;
import com.dgmoonlabs.cms.domain.government.satisfaction.entity.Satisfaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SatisfactionCustomRepository {
    Page<Satisfaction> find(SatisfactionRequest satisfactionRequest, Pageable pageable);

    List<Satisfaction> find(SatisfactionRequest satisfactionRequest);
}

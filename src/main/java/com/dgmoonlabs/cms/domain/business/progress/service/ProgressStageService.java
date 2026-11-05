package com.dgmoonlabs.cms.domain.business.progress.service;

import com.dgmoonlabs.cms.domain.business.progress.constant.ProgressTargetType;
import com.dgmoonlabs.cms.domain.business.progress.dto.ProgressDtos.*;
import com.dgmoonlabs.cms.domain.business.progress.entity.ProgressStageEvent;
import com.dgmoonlabs.cms.domain.business.progress.entity.ProgressStageTemplate;
import com.dgmoonlabs.cms.domain.business.progress.repository.ProgressStageEventRepository;
import com.dgmoonlabs.cms.domain.business.progress.repository.ProgressStageTemplateRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressStageService {
    private final ProgressStageTemplateRepository templateRepository;
    private final ProgressStageEventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<StageTemplateResponse> listTemplates(final ProgressTargetType targetType) {
        return templateRepository.findAllByTargetTypeOrderBySequenceAsc(targetType).stream()
                .map(StageTemplateResponse::from).toList();
    }

    @Transactional
    public StageTemplateResponse createTemplate(final ProgressTargetType targetType, final StageTemplateSaveRequest request) {
        if (templateRepository.existsByTargetTypeAndCode(targetType, request.code())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PROGRESS_STAGE_CODE);
        }
        return StageTemplateResponse.from(templateRepository.save(ProgressStageTemplate.builder()
                .targetType(targetType)
                .code(request.code())
                .label(request.label())
                .sequence(request.sequence())
                .build()));
    }

    @Transactional
    public StageTemplateResponse updateTemplate(final Long id, final StageTemplateSaveRequest request) {
        ProgressStageTemplate template = templateRepository.findById(id).orElseThrow(BusinessException::notFound);
        template.update(request.label(), request.sequence());
        return StageTemplateResponse.from(template);
    }

    @Transactional
    public void deleteTemplate(final Long id) {
        templateRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProgressResponse get(final ProgressTargetType targetType, final Long targetId) {
        List<StageEventResponse> history = eventRepository.findAllByTargetTypeAndTargetIdOrderByIdAsc(targetType, targetId)
                .stream().map(StageEventResponse::from).toList();
        String current = history.isEmpty() ? null : history.get(history.size() - 1).stageCode();
        return new ProgressResponse(listTemplates(targetType), history, current);
    }

    @Transactional
    public StageEventResponse advance(final ProgressTargetType targetType, final Long targetId, final AdvanceRequest request,
                                      final String actorName) {
        ProgressStageTemplate template = templateRepository.findAllByTargetTypeOrderBySequenceAsc(targetType).stream()
                .filter(t -> t.getCode().equals(request.stageCode())).findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PROGRESS_STAGE));
        ProgressStageEvent event = eventRepository.save(ProgressStageEvent.builder()
                .targetType(targetType)
                .targetId(targetId)
                .stageCode(template.getCode())
                .stageLabel(template.getLabel())
                .memo(blankToNull(request.memo()))
                .actorName(actorName)
                .build());
        return StageEventResponse.from(event);
    }

    private static String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

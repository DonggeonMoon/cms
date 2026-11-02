package com.dgmoonlabs.cms.domain.map.service;

import com.dgmoonlabs.cms.domain.map.dto.MapDtos.*;
import com.dgmoonlabs.cms.domain.map.entity.Branch;
import com.dgmoonlabs.cms.domain.map.entity.MapSetting;
import com.dgmoonlabs.cms.domain.map.repository.BranchRepository;
import com.dgmoonlabs.cms.domain.map.repository.MapSettingRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapService {
    private final MapSettingRepository settingRepository;
    private final BranchRepository branchRepository;

    @Transactional(readOnly = true)
    public PublicMapResponse getPublicMap() {
        MapSetting setting = currentSetting();
        List<BranchResponse> branches = setting.isBranchesEnabled()
                ? branchRepository.findAllByIsUsingTrueAndDeletedAtIsNullOrderBySequenceAscIdAsc().stream()
                .map(BranchResponse::from).toList()
                : null;
        return new PublicMapResponse(
                setting.getProvider(),
                setting.apiKey(),
                setting.isDirectionsEnabled() ? Office.from(setting) : null,
                branches
        );
    }

    @Transactional(readOnly = true)
    public MapSettingResponse getSetting() {
        return MapSettingResponse.from(currentSetting());
    }

    @Transactional
    public MapSettingResponse updateSetting(final MapSettingRequest request) {
        if (request.directionsEnabled() && !request.hasOfficeLocation()) {
            throw new BusinessException(ErrorCode.MAP_LOCATION_REQUIRED);
        }
        MapSetting setting = settingRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> settingRepository.save(MapSetting.empty()));
        setting.update(request);
        return MapSettingResponse.from(setting);
    }

    @Transactional(readOnly = true)
    public Page<BranchResponse> searchBranches(final String keyword, final Pageable pageable) {
        return branchRepository.findAllByDeletedAtIsNullAndNameContainingIgnoreCaseOrderBySequenceAscIdAsc(
                keyword == null ? "" : keyword, pageable).map(BranchResponse::from);
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranch(final Long id) {
        return BranchResponse.from(findBranch(id));
    }

    @Transactional
    public Long createBranch(final BranchSaveRequest request) {
        requireBranchesEnabled();
        return branchRepository.save(Branch.from(request)).getId();
    }

    @Transactional
    public void updateBranch(final Long id, final BranchSaveRequest request) {
        requireBranchesEnabled();
        findBranch(id).update(request);
    }

    @Transactional
    public void deleteBranch(final Long id) {
        findBranch(id).delete();
    }

    private void requireBranchesEnabled() {
        if (!currentSetting().isBranchesEnabled()) {
            throw new BusinessException(ErrorCode.BRANCHES_DISABLED);
        }
    }

    private MapSetting currentSetting() {
        return settingRepository.findFirstByOrderByIdAsc().orElseGet(MapSetting::empty);
    }

    private Branch findBranch(final Long id) {
        return branchRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }
}

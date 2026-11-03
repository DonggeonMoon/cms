package com.dgmoonlabs.cms.domain.common.guestposting;

import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuestPostingService {
    private final GuestPostingSettingRepository repository;

    @Transactional(readOnly = true)
    public GuestPostingPolicy policy(final SiteFeature feature) {
        return repository.findFirstByOrderByIdAsc().orElseGet(GuestPostingSetting::defaults).policy(feature);
    }

    @Transactional(readOnly = true)
    public boolean allows(final SiteFeature feature) {
        return policy(feature).allowed();
    }

    @Transactional
    public GuestPostingPolicy change(final SiteFeature feature, final GuestPostingPolicy policy) {
        GuestPostingSetting setting = repository.findFirstByOrderByIdAsc()
                .orElseGet(() -> repository.save(GuestPostingSetting.defaults()));
        setting.change(feature, policy);
        return setting.policy(feature);
    }
}

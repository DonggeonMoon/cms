package com.dgmoonlabs.cms.domain.admin.ipaccess.service;

import com.dgmoonlabs.cms.domain.admin.ipaccess.dto.AllowedIpDtos.AllowedIpResponse;
import com.dgmoonlabs.cms.domain.admin.ipaccess.dto.AllowedIpDtos.AllowedIpSaveRequest;
import com.dgmoonlabs.cms.domain.admin.ipaccess.dto.AllowedIpDtos.AllowedIpSettings;
import com.dgmoonlabs.cms.domain.admin.ipaccess.entity.AdminIpSetting;
import com.dgmoonlabs.cms.domain.admin.ipaccess.entity.AllowedIp;
import com.dgmoonlabs.cms.domain.admin.ipaccess.repository.AdminIpSettingRepository;
import com.dgmoonlabs.cms.domain.admin.ipaccess.repository.AllowedIpRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 관리자 화면·API를 허용한 IP에서만 쓰게 한다. 관리자 요청마다 확인하므로 목록을 잠깐 메모리에 둔다.
 * 서버를 여러 대 띄우면 다른 서버의 변경은 CACHE_MILLIS 뒤에 반영된다.
 */
@Service
public class AdminIpAccessService {
    private static final long CACHE_MILLIS = 10_000;

    private final AllowedIpRepository allowedIpRepository;
    private final AdminIpSettingRepository settingRepository;
    private final boolean forceDisabled;
    private volatile Snapshot snapshot;

    public AdminIpAccessService(final AllowedIpRepository allowedIpRepository, final AdminIpSettingRepository settingRepository,
                                @Value("${cms.security.admin-ip-restriction-disabled:false}") final boolean forceDisabled) {
        this.allowedIpRepository = allowedIpRepository;
        this.settingRepository = settingRepository;
        this.forceDisabled = forceDisabled;
    }

    private record Snapshot(boolean enabled, List<IpRange> ranges, long loadedAt) {
        boolean allows(final String ip) {
            return !enabled || ranges.stream().anyMatch(range -> range.contains(ip));
        }
    }

    public boolean isAllowed(final String ip) {
        if (forceDisabled) {
            return true;
        }
        Snapshot current = snapshot;
        if (current == null || System.currentTimeMillis() - current.loadedAt() > CACHE_MILLIS) {
            current = new Snapshot(isEnabled(), allowedIpRepository.findAllByOrderByIdAsc().stream()
                    .map(entry -> IpRange.parse(entry.getAddress())).toList(), System.currentTimeMillis());
            snapshot = current;
        }
        return current.allows(ip);
    }

    public void requireAllowed(final String ip) {
        if (!isAllowed(ip)) {
            throw new BusinessException(ErrorCode.ADMIN_IP_NOT_ALLOWED);
        }
    }

    @Transactional(readOnly = true)
    public AllowedIpSettings getSettings(final String currentIp) {
        return settings(currentIp);
    }

    @Transactional
    public AllowedIpSettings create(final AllowedIpSaveRequest request, final String currentIp) {
        String address = IpRange.parse(request.address()).canonical();
        if (allowedIpRepository.existsByAddress(address)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ALLOWED_IP);
        }
        List<String> after = new ArrayList<>(addresses());
        after.add(address);
        requireNoLockout(isEnabled(), after, currentIp);
        allowedIpRepository.save(AllowedIp.builder().address(address).memo(request.memoOrEmpty()).build());
        return changed(currentIp);
    }

    @Transactional
    public AllowedIpSettings update(final Long id, final AllowedIpSaveRequest request, final String currentIp) {
        AllowedIp ip = allowedIpRepository.findById(id).orElseThrow(BusinessException::notFound);
        String address = IpRange.parse(request.address()).canonical();
        if (allowedIpRepository.existsByAddressAndIdNot(address, id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ALLOWED_IP);
        }
        List<String> after = allowedIpRepository.findAllByOrderByIdAsc().stream()
                .map(entry -> entry.getId().equals(id) ? address : entry.getAddress()).toList();
        requireNoLockout(isEnabled(), after, currentIp);
        ip.update(address, request.memoOrEmpty());
        return changed(currentIp);
    }

    @Transactional
    public AllowedIpSettings delete(final Long id, final String currentIp) {
        AllowedIp ip = allowedIpRepository.findById(id).orElseThrow(BusinessException::notFound);
        List<String> after = allowedIpRepository.findAllByOrderByIdAsc().stream()
                .filter(entry -> !entry.getId().equals(id)).map(AllowedIp::getAddress).toList();
        requireNoLockout(isEnabled(), after, currentIp);
        allowedIpRepository.delete(ip);
        return changed(currentIp);
    }

    @Transactional
    public AllowedIpSettings changeEnabled(final boolean enabled, final String currentIp) {
        requireNoLockout(enabled, addresses(), currentIp);
        settingRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> settingRepository.save(AdminIpSetting.builder().enabled(false).build()))
                .changeEnabled(enabled);
        return changed(currentIp);
    }

    /** 바꾼 결과로 지금 접속한 IP가 막히면 저장하지 않는다. 원격에서 스스로 잠그는 일을 막기 위해서다. */
    private static void requireNoLockout(final boolean enabled, final List<String> addresses, final String currentIp) {
        if (enabled && !matchesAny(addresses, currentIp)) {
            throw new BusinessException(ErrorCode.ADMIN_IP_LOCKOUT);
        }
    }

    private AllowedIpSettings changed(final String currentIp) {
        allowedIpRepository.flush();
        // 커밋 전에 다른 요청이 옛 목록을 다시 읽어 둘 수 있어 커밋 뒤에도 한 번 더 비운다.
        snapshot = null;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    snapshot = null;
                }
            });
        }
        return settings(currentIp);
    }

    private AllowedIpSettings settings(final String currentIp) {
        List<AllowedIp> entries = allowedIpRepository.findAllByOrderByIdAsc();
        return new AllowedIpSettings(isEnabled(), forceDisabled, currentIp,
                matchesAny(entries.stream().map(AllowedIp::getAddress).toList(), currentIp),
                entries.stream().map(AllowedIpResponse::from).toList());
    }

    private List<String> addresses() {
        return allowedIpRepository.findAllByOrderByIdAsc().stream().map(AllowedIp::getAddress).toList();
    }

    private boolean isEnabled() {
        return settingRepository.findFirstByOrderByIdAsc().map(AdminIpSetting::isEnabled).orElse(false);
    }

    private static boolean matchesAny(final List<String> addresses, final String ip) {
        return addresses.stream().anyMatch(address -> IpRange.parse(address).contains(ip));
    }
}

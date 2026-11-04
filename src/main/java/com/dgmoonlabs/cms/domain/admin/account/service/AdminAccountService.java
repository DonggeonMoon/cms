package com.dgmoonlabs.cms.domain.admin.account.service;

import com.dgmoonlabs.cms.domain.admin.account.constant.AdminArea;
import com.dgmoonlabs.cms.domain.admin.account.dto.AdminAccountDtos.*;
import com.dgmoonlabs.cms.domain.admin.account.entity.AdminGrade;
import com.dgmoonlabs.cms.domain.admin.account.repository.AdminGradeRepository;
import com.dgmoonlabs.cms.domain.common.user.entity.admin.entity.Admin;
import com.dgmoonlabs.cms.domain.common.user.repository.AdminRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAccountService implements ApplicationRunner {
    private static final String SUPER_GRADE_NAME = "최고 관리자";
    /** 등급이 없는 관리자(등급 기능 이전부터 있던 계정)는 지금까지처럼 보안 영역만 빼고 쓸 수 있다. */
    private static final EnumSet<AdminArea> UNGRADED_AREAS = EnumSet.complementOf(EnumSet.of(AdminArea.SECURITY));

    private final AdminRepository adminRepository;
    private final AdminGradeRepository gradeRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        ensureSuperAdmin();
    }

    /** 최고 관리자가 한 명도 없으면 가장 먼저 만든 관리자를 최고 관리자로 정해, 관리자 관리에 아무도 못 들어가는 일을 막는다. */
    @Transactional
    public void ensureSuperAdmin() {
        AdminGrade superGrade = superGrade();
        List<Admin> admins = adminRepository.findAllByDeletedAtIsNullOrderByIdAsc();
        if (!admins.isEmpty() && admins.stream().noneMatch(admin -> superGrade.getId().equals(admin.getGradeId()))) {
            admins.getFirst().changeGrade(superGrade.getId());
        }
    }

    @Transactional(readOnly = true)
    public MyPermissions permissions(final Long adminId) {
        AdminGrade grade = gradeOf(findAdmin(adminId));
        if (grade == null) {
            return new MyPermissions(false, null, List.copyOf(UNGRADED_AREAS));
        }
        List<AdminArea> areas = Arrays.stream(AdminArea.values()).filter(grade::allows).toList();
        return new MyPermissions(grade.isSuperAdmin(), grade.getName(), areas);
    }

    @Transactional(readOnly = true)
    public boolean canAccess(final Long adminId, final String segment) {
        if (AdminArea.ALWAYS.contains(segment)) {
            return true;
        }
        MyPermissions permissions = permissions(adminId);
        if (permissions.superAdmin()) {
            return true;
        }
        return AdminArea.ofSegment(segment).map(permissions.areas()::contains).orElse(false);
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> grades() {
        return gradeRepository.findAllByDeletedAtIsNullOrderBySuperAdminDescIdAsc().stream()
                .map(grade -> GradeResponse.of(grade, adminRepository.countByGradeIdAndDeletedAtIsNull(grade.getId())))
                .toList();
    }

    @Transactional
    public Long createGrade(final GradeSaveRequest request) {
        return gradeRepository.save(AdminGrade.builder()
                .name(request.name().trim())
                .description(request.description())
                .areas(request.areas().stream().distinct().sorted().toList())
                .build()).getId();
    }

    @Transactional
    public void updateGrade(final Long id, final GradeSaveRequest request) {
        findGrade(id).update(request.name().trim(), request.description(), request.areas().stream().distinct().sorted().toList());
    }

    @Transactional
    public void deleteGrade(final Long id) {
        AdminGrade grade = findGrade(id);
        if (grade.isSuperAdmin() || adminRepository.countByGradeIdAndDeletedAtIsNull(id) > 0) {
            throw new BusinessException(ErrorCode.ADMIN_GRADE_IN_USE);
        }
        grade.delete();
    }

    @Transactional(readOnly = true)
    public List<AdminResponse> admins() {
        Map<Long, AdminGrade> grades = gradeRepository.findAllByDeletedAtIsNullOrderBySuperAdminDescIdAsc().stream()
                .collect(Collectors.toMap(AdminGrade::getId, Function.identity()));
        return adminRepository.findAllByDeletedAtIsNullOrderByIdAsc().stream()
                .map(admin -> AdminResponse.of(admin, admin.getGradeId() == null ? null : grades.get(admin.getGradeId()),
                        admin.isLocked(securityProperties.maxLoginTryCount())))
                .toList();
    }

    @Transactional
    public Long createAdmin(final AdminCreateRequest request) {
        if (adminRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }
        Long gradeId = request.gradeId() == null ? null : findGrade(request.gradeId()).getId();
        return adminRepository.save(Admin.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name().trim())
                .gradeId(gradeId)
                .build()).getId();
    }

    @Transactional
    public void assignGrade(final Long adminId, final Long gradeId) {
        Admin admin = findAdmin(adminId);
        AdminGrade next = gradeId == null ? null : findGrade(gradeId);
        if (isSuper(admin) && (next == null || !next.isSuperAdmin())) {
            requireAnotherSuper();
        }
        admin.changeGrade(next == null ? null : next.getId());
    }

    @Transactional
    public void unlock(final Long adminId) {
        findAdmin(adminId).unlock();
    }

    @Transactional
    public void deleteAdmin(final Long actorId, final Long adminId) {
        Admin admin = findAdmin(adminId);
        if (admin.getId().equals(actorId)) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE_SELF);
        }
        if (isSuper(admin)) {
            requireAnotherSuper();
        }
        admin.delete();
    }

    public List<AreaOption> areaOptions() {
        return Arrays.stream(AdminArea.values()).map(area -> new AreaOption(area, area.getLabel())).toList();
    }

    private void requireAnotherSuper() {
        if (adminRepository.countByGradeIdAndDeletedAtIsNull(superGrade().getId()) <= 1) {
            throw new BusinessException(ErrorCode.LAST_SUPER_ADMIN);
        }
    }

    private boolean isSuper(final Admin admin) {
        AdminGrade grade = gradeOf(admin);
        return grade != null && grade.isSuperAdmin();
    }

    private AdminGrade superGrade() {
        return gradeRepository.findFirstBySuperAdminTrueAndDeletedAtIsNull().orElseGet(() -> gradeRepository.save(AdminGrade.builder()
                .name(SUPER_GRADE_NAME)
                .description("모든 관리 영역과 관리자 관리를 쓸 수 있습니다.")
                .superAdmin(true)
                .areas(List.of(AdminArea.values()))
                .build()));
    }

    private AdminGrade gradeOf(final Admin admin) {
        return admin.getGradeId() == null ? null : gradeRepository.findByIdAndDeletedAtIsNull(admin.getGradeId()).orElse(null);
    }

    private AdminGrade findGrade(final Long id) {
        return gradeRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(BusinessException::notFound);
    }

    private Admin findAdmin(final Long id) {
        return adminRepository.findById(id).filter(admin -> admin.getDeletedAt() == null).orElseThrow(BusinessException::notFound);
    }
}

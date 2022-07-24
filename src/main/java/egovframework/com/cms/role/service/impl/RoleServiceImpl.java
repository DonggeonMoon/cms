package egovframework.com.cms.role.service.impl;

import egovframework.com.cms.member.model.Member;
import egovframework.com.cms.role.dto.RoleSearch;
import egovframework.com.cms.role.model.Role;
import egovframework.com.cms.role.service.RoleService;

import java.util.List;
import java.util.Map;

public class RoleServiceImpl implements RoleService {
    @Override
    public List<Role> getRoleList(RoleSearch roleSearch) {
        return null;
    }

    @Override
    public Role getRole(Role role) {
        return null;
    }

    @Override
    public int insertRole(Role role) {
        return 0;
    }

    @Override
    public int updateRole(Role role) {
        return 0;
    }

    @Override
    public int deleteRole(List<Role> roleList) {
        return 0;
    }

    @Override
    public int deleteRole(String[] roleCodes) {
        return 0;
    }

    @Override
    public boolean isCurrentUserAccessibleResource(Member currentUser, String requestUri, Map<String, String[]> parameterMap, String method) {
        return false;
    }

    @Override
    public Role getJoinRole() {
        return null;
    }

    @Override
    public List<Role> getRoleWithCapList(String capId) {
        return null;
    }
}

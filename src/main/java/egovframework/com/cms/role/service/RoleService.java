package egovframework.com.cms.role.service;

import egovframework.com.cms.member.model.Member;
import egovframework.com.cms.role.dto.RoleSearch;
import egovframework.com.cms.role.model.Role;

import java.util.List;
import java.util.Map;

public interface RoleService {
    public List<Role> getRoleList(RoleSearch roleSearch);

    public Role getRole(Role role);

    public int insertRole(Role role);

    public int updateRole(Role role);

    public int deleteRole(List<Role> roleList);

    public int deleteRole(String[] roleCodes);

    public boolean isCurrentUserAccessibleResource(Member currentUser, String requestUri, Map<String, String[]> parameterMap, String method);

    public Role getJoinRole();

    public List<Role> getRoleWithCapList(String capId);
}

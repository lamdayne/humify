package com.lamdayne.humify.auth.service;

import com.lamdayne.humify.auth.entity.Role;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.user.entity.User;

import java.util.List;
import java.util.Set;

public interface RoleAccessService {

    List<Role> resolveRoles(List<Long> rawIds);

    void assignRoles(User user, List<Long> roleIds);

    Set<String> findAllRoleNames(UserPrincipal userPrincipal);

    void assignCompanyAdmin(User user);

    void assignEmployeeRole(User user);

}

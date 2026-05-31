package com.lamdayne.humify.auth.service;

import com.lamdayne.humify.auth.dto.request.CreateRoleRequest;
import com.lamdayne.humify.auth.dto.response.RoleResponse;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;

public interface RoleService {

    RoleResponse createRole(UserPrincipal userPrincipal, CreateRoleRequest request);

    void deleteRole(UserPrincipal userPrincipal, Long roleId);

}

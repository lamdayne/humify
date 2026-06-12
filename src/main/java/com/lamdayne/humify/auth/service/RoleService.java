package com.lamdayne.humify.auth.service;

import com.lamdayne.humify.auth.dto.request.CreateRoleRequest;
import com.lamdayne.humify.auth.dto.request.UpdateRoleRequest;
import com.lamdayne.humify.auth.dto.response.RoleResponse;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.PageResponse;

public interface RoleService {

    RoleResponse createRole(UserPrincipal userPrincipal, CreateRoleRequest request);

    void deleteRole(UserPrincipal userPrincipal, Long roleId);

    RoleResponse updateRole(UserPrincipal userPrincipal, Long roleId, UpdateRoleRequest request);

    PageResponse<RoleResponse> findAll(int page, int size, String... sorts);

    RoleResponse findById(Long roleId);

}

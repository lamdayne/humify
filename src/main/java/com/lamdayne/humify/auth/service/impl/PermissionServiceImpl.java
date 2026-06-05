package com.lamdayne.humify.auth.service.impl;

import com.lamdayne.humify.auth.dto.response.PermissionResponse;
import com.lamdayne.humify.auth.entity.Permission;
import com.lamdayne.humify.auth.enums.PermissionEnum;
import com.lamdayne.humify.auth.enums.PermissionModule;
import com.lamdayne.humify.auth.repository.PermissionRepository;
import com.lamdayne.humify.auth.service.PermissionService;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    public PageResponse<PermissionResponse> getPermissions(int page, int size, String... sorts) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        Page<Permission> permissionPage = permissionRepository.findAll(pageable);

        List<PermissionResponse> permissions = permissionPage.stream()
                .filter(permission -> {
                    String name = permission.getName();

                    if (name.equals(PermissionEnum.FULL_ACCESS.name())) return false;

                    if (permission.getModule().equals(PermissionModule.SYSTEM.name())) return false;

                    if (permission.getModule().equals(PermissionModule.PERMISSION.name())) return false;

                    return !name.endsWith("_FULL");
                })
                .map(PermissionResponse::from)
                .toList();

        return PageResponse.<PermissionResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalElements(permissionPage.getTotalElements())
                .totalPages(permissionPage.getTotalPages())
                .items(permissions)
                .build();
    }
}

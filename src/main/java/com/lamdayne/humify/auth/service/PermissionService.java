package com.lamdayne.humify.auth.service;

import com.lamdayne.humify.auth.dto.response.PermissionResponse;
import com.lamdayne.humify.common.response.PageResponse;

public interface PermissionService {

    PageResponse<PermissionResponse> getPermissions(int page, int size, String... sorts);

}

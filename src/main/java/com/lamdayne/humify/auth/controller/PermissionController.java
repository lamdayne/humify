package com.lamdayne.humify.auth.controller;

import com.lamdayne.humify.auth.dto.response.PermissionResponse;
import com.lamdayne.humify.auth.service.PermissionService;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'PERMISSION_READ', 'PERMISSION_FULL')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> getAllPermissions(
            @RequestParam(defaultValue = "0", required = false) @Min(value = 0, message = "PAGE_NO_INVALID") int page,
            @RequestParam(defaultValue = "50", required = false) @Min(value = 10, message = "PAGE_SIZE_INVALID") int size,
            @RequestParam(required = false) String... sorts
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.PERMISSION_READ_SUCCESS,
                        permissionService.getPermissions(page, size, sorts)
                ));
    }

}

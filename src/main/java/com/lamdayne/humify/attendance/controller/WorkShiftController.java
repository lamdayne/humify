package com.lamdayne.humify.attendance.controller;

import com.lamdayne.humify.attendance.dto.request.CreateWorkShiftRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateWorkShiftRequest;
import com.lamdayne.humify.attendance.dto.response.WorkShiftResponse;
import com.lamdayne.humify.attendance.service.WorkShiftService;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.common.util.PageableUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/work-shifts")
public class WorkShiftController {

    private final WorkShiftService workShiftService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkShiftResponse>> createWorkShift(
            @Valid @RequestBody CreateWorkShiftRequest request
    ) {
        WorkShiftResponse response = workShiftService.createWorkShift(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.WORK_SHIFT_CREATE_SUCCESS, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WorkShiftResponse>>> getWorkShifts(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(required = false) String[] sorts,
            @RequestParam(required = false, name = "shift") String[] searchParams
    ) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        PageResponse<WorkShiftResponse> response = workShiftService.getWorkShifts(pageable, searchParams);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.WORK_SHIFT_READ_SUCCESS, response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkShiftResponse>> getWorkShiftDetail(@PathVariable Long id) {
        WorkShiftResponse response = workShiftService.getWorkShiftDetail(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.WORK_SHIFT_READ_SUCCESS, response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkShiftResponse>> updateWorkShift(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkShiftRequest request
    ) {
        WorkShiftResponse response = workShiftService.updateWorkShift(id, request);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.WORK_SHIFT_UPDATE_SUCCESS, response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkShift(@PathVariable Long id) {
        workShiftService.deactivateWorkShift(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.WORK_SHIFT_DELETE_SUCCESS, null));
    }
}
package com.lamdayne.humify.project.controller;

import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.project.dto.request.CreateColumnRequest;
import com.lamdayne.humify.project.dto.request.ReorderColumnsRequest;
import com.lamdayne.humify.project.dto.request.UpdateColumnRequest;
import com.lamdayne.humify.project.dto.response.BoardColumnResponse;
import com.lamdayne.humify.project.service.BoardColumnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BoardColumnController {

    private final BoardColumnService boardColumnService;

    @GetMapping("/projects/{projectId}/columns")
    public ResponseEntity<ApiResponse<List<BoardColumnResponse>>> getColumns(
            @PathVariable(name = "projectId") Long projectId
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.COLUMN_READ_SUCCESS,
                        boardColumnService.getColumns(projectId)
                ));
    }

    @PostMapping("/projects/{projectId}/columns")
    public ResponseEntity<ApiResponse<BoardColumnResponse>> createColumn(
            @PathVariable(name = "projectId") Long projectId,
            @RequestBody @Valid CreateColumnRequest request
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.COLUMN_CREATE_SUCCESS,
                        boardColumnService.createColumn(projectId, request)
                ));
    }

    @PutMapping("/projects/{projectId}/columns/reorder")
    public ResponseEntity<ApiResponse<List<BoardColumnResponse>>> reorderColumns(
            @PathVariable(name = "projectId") Long projectId,
            @RequestBody @Valid ReorderColumnsRequest request
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.COLUMN_REORDER_SUCCESS,
                        boardColumnService.reorderColumns(projectId, request)
                ));
    }

    @PutMapping("/columns/{id}")
    public ResponseEntity<ApiResponse<BoardColumnResponse>> updateColumn(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateColumnRequest request
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.COLUMN_UPDATE_SUCCESS,
                        boardColumnService.updateColumn(id, request)
                ));
    }

    @DeleteMapping("/columns/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteColumn(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "moveToColumnId", required = false) Long moveToColumnId
    ) {
        boardColumnService.deleteColumn(id, moveToColumnId);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.COLUMN_DELETE_SUCCESS));
    }
}
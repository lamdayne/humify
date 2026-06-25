package com.lamdayne.humify.project.controller;

import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.project.dto.request.UpdateColumnRequest;
import com.lamdayne.humify.project.dto.response.BoardColumnResponse;
import com.lamdayne.humify.project.service.BoardColumnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BoardColumnController {

    private final BoardColumnService boardColumnService;
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
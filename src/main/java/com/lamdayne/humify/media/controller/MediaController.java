package com.lamdayne.humify.media.controller;

import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.media.dto.response.PresignedUploadResponse;
import com.lamdayne.humify.media.dto.response.UploadResponse;
import com.lamdayne.humify.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UploadResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subfolder", defaultValue = "general") String subfolder
    ) {
        UploadResponse response = mediaService.upload(file, subfolder);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.FILE_UPLOAD_SUCCESS, response));
    }

    @GetMapping("/presign")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PresignedUploadResponse>> getPresignedUrl(
            @RequestParam(value = "subfolder", defaultValue = "general") String subfolder
    ) {
        PresignedUploadResponse response = mediaService.getPresignedUrl(subfolder);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.FILE_UPLOAD_SUCCESS, response));
    }
}

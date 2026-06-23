package com.lamdayne.humify.media.service;

import com.lamdayne.humify.media.dto.response.PresignedUploadResponse;
import com.lamdayne.humify.media.dto.response.UploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    UploadResponse upload(MultipartFile file, String subfolder);
    UploadResponse upload(MultipartFile file);
    PresignedUploadResponse getPresignedUrl(String subfolder);
}

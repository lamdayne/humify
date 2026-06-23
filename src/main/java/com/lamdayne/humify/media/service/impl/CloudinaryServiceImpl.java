package com.lamdayne.humify.media.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.media.dto.response.PresignedUploadResponse;
import com.lamdayne.humify.media.dto.response.UploadResponse;
import com.lamdayne.humify.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements MediaService {

    private final Cloudinary cloudinary;

    @Override
    public UploadResponse upload(MultipartFile file, String subfolder) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }
        try {
            String folderPath = resolveFolderPath(subfolder);
            Map<?, ?> options = ObjectUtils.asMap(
                    "folder", folderPath,
                    "resource_type", "auto"
            );
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            String url = uploadResult.get("secure_url").toString();
            String publicId = uploadResult.get("public_id").toString();

            return UploadResponse.builder()
                    .url(url)
                    .publicId(publicId)
                    .build();
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public UploadResponse upload(MultipartFile file) {
        return upload(file, "general");
    }

    @Override
    public PresignedUploadResponse getPresignedUrl(String subfolder) {
        String folderPath = resolveFolderPath(subfolder);

        long timestamp = System.currentTimeMillis() / 1000L;
        Map<String, Object> paramsToSign = new TreeMap<>();
        paramsToSign.put("folder", folderPath);
        paramsToSign.put("timestamp", timestamp);

        String signature = cloudinary.apiSignRequest(paramsToSign, cloudinary.config.apiSecret, 1);
        String uploadUrl = String.format("https://api.cloudinary.com/v1_1/%s/auto/upload", cloudinary.config.cloudName);

        return PresignedUploadResponse.builder()
                .signature(signature)
                .timestamp(timestamp)
                .apiKey(cloudinary.config.apiKey)
                .cloudName(cloudinary.config.cloudName)
                .folder(folderPath)
                .uploadUrl(uploadUrl)
                .build();
    }

    private String resolveFolderPath(String subfolder) {
        Long companyId = getCurrentCompanyId();
        String folderPath;
        if (companyId != null) {
            folderPath = "humify/companies/company_" + companyId;
        } else {
            folderPath = "humify/system";
        }
        if (subfolder != null && !subfolder.trim().isEmpty()) {
            folderPath = String.format("%s/%s", folderPath, subfolder.trim());
        }
        return folderPath;
    }

    private Long getCurrentCompanyId() {
        Long companyId = CompanyContext.getCompanyId();
        if (companyId == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
                companyId = principal.getCompanyId();
            }
        }
        return companyId;
    }
}

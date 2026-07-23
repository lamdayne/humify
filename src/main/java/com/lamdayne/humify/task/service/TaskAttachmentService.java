package com.lamdayne.humify.task.service;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.task.dto.request.AddAttachmentRequest;
import com.lamdayne.humify.task.dto.response.AttachmentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TaskAttachmentService {

    AttachmentResponse addAttachment(UserPrincipal userPrincipal, Long taskId, MultipartFile file);

    void deleteAttachment(Long attachmentId);

    AttachmentResponse addAttachment(UserPrincipal userPrincipal, Long taskId, AddAttachmentRequest request);

    List<AttachmentResponse> getAttachments(Long taskId);

}
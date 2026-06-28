package com.lamdayne.humify.task.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
@Builder
public class AttachmentResponse {
    private Long id;
    private Long taskId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private UserShortResponse uploadedBy;
    private Instant createdAt;
}
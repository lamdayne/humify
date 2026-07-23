package com.lamdayne.humify.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddAttachmentRequest {

    @NotBlank(message = "FILE_NAME_NOT_BLANK")
    private String fileName;

    @NotBlank(message = "FILE_URL_NOT_BLANK")
    private String fileUrl;

    @NotNull(message = "FILE_SIZE_NOT_NULL")
    private Long fileSize;

}

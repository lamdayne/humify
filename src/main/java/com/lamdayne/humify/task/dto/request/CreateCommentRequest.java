package com.lamdayne.humify.task.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {

    private Long parentId;

    @NotBlank(message = "COMMENT_CONTENT_REQUIRED")
    private String content;
}

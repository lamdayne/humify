package com.lamdayne.humify.task.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long taskId;
    private Long parentId;
    private String content;
    private UserShortResponse author;
    private Instant createdAt;
    private Instant updatedAt;
}

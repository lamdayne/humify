package com.lamdayne.humify.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
public class UserResponse implements Serializable {
    private String email;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}

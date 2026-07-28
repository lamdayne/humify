package com.lamdayne.humify.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class UserResponse implements Serializable {
    private Long id;
    private String email;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private List<UserRoleResponse> roles;
}

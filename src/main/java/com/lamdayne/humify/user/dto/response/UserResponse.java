package com.lamdayne.humify.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class UserResponse implements Serializable {
    private String email;
    private String password;
}

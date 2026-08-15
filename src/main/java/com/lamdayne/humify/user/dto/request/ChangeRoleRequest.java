package com.lamdayne.humify.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleRequest {

    @NotEmpty(message = "ROLE_ID_REQUIRED")
    private List<Long> roleIds;

}

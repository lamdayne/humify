package com.lamdayne.humify.project.dto.request;

import com.lamdayne.humify.common.validator.EnumValue;
import com.lamdayne.humify.project.enums.ProjectRoleCode;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberRoleRequest implements Serializable {

    @EnumValue(name = "project_role_code", message = "PROJECT_ROLE_INVALID", enumClass = ProjectRoleCode.class)
    private String code;

}

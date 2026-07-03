package com.lamdayne.humify.project.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateInvitationResponse {
    private boolean isValid;
    private Long projectId;
    private String projectName;
    private String roleCode;
    private String roleName;
    private String email;
    private String companyCode;
}

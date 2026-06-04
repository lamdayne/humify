package com.lamdayne.humify.branch.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Builder
public class BranchResponse implements Serializable {
    private Long id;
    private Long companyId;
    private String branchCode;
    private String name;
    private String field;
    private String website;
    private String address;
    private String status;
    private Integer standardHoursPerDay;
    private Instant createdAt;
    private Instant updatedAt;
}
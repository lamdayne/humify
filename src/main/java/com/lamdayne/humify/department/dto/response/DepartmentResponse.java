package com.lamdayne.humify.department.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Builder
public class DepartmentResponse implements Serializable {
    private Long id;
    private Long branchId;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

}

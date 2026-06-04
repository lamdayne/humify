package com.lamdayne.humify.branch.entity;

import com.lamdayne.humify.branch.enums.BranchStatus;
import com.lamdayne.humify.common.base.BaseEntity;
import com.lamdayne.humify.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "branches")
public class Branch extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 50)
    private String branchCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String field;

    private String website;

    private String address;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "branch_status", nullable = false)
    private BranchStatus status = BranchStatus.ACTIVE;

    @Builder.Default
    @Column(nullable = false)
    private Integer standardHoursPerDay = 8;

}

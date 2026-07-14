package com.lamdayne.humify.attendance.entity;

import com.lamdayne.humify.attendance.enums.AttendanceStatus;
import com.lamdayne.humify.attendance.enums.CheckedStatus;
import com.lamdayne.humify.common.base.BaseEntity;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendances")
public class Attendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_shift_id")
    private WorkShift workShift;

    @Column(nullable = false)
    private LocalDate workDate;

    private Instant checkInTime;

    private Instant checkOutTime;

    @Builder.Default
    @Column(precision = 5, scale = 2)
    private BigDecimal workedHours = BigDecimal.ZERO;

    @Builder.Default
    private Integer lateMinutes = 0;

    @Builder.Default
    private Integer earlyMinutes = 0;

    @Builder.Default
    @Column(precision = 5, scale = 2)
    private BigDecimal otHours = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 3, scale = 2)
    private BigDecimal workPoints = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "checked_status", nullable = false)
    private CheckedStatus checkedStatus = CheckedStatus.NOT_CHECKED;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "attendance_status", nullable = false)
    private AttendanceStatus status = AttendanceStatus.ABSENT;

    @Builder.Default
    private Boolean isModified = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by_id")
    private User modifiedBy;

    private String modificationReason;

}

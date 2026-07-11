package com.lamdayne.humify.attendance.entity;

import com.lamdayne.humify.attendance.enums.AttendanceCorrectionStatus;
import com.lamdayne.humify.common.base.BaseEntity;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attendance_corrections")
public class AttendanceCorrection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id")
    private Attendance attendance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate correctionDate;

    private Instant requestedCheckIn;

    private Instant requestedCheckOut;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "attendance_correction_status", nullable = false)
    private AttendanceCorrectionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    private Instant approvedAt;

    private String approverNote;

}

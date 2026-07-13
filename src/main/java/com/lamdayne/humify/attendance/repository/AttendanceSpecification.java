package com.lamdayne.humify.attendance.repository;

import com.lamdayne.humify.attendance.entity.Attendance;
import com.lamdayne.humify.attendance.enums.AttendanceStatus;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;

public class AttendanceSpecification {

    public static Specification<Attendance> filterHR(Long companyId, Long employeeId, LocalDate start, LocalDate end, AttendanceStatus status) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            predicate = cb.and(predicate, cb.equal(root.get("company").get("id"), companyId));
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("workDate"), start));
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("workDate"), end));

            if (employeeId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("employee").get("id"), employeeId));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            return predicate;
        };
    }
}
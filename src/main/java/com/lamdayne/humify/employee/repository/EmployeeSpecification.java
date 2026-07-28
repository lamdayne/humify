package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.search.GenericSpecificationBuilder;
import com.lamdayne.humify.common.search.SpecSearchCriteria;
import com.lamdayne.humify.employee.entity.Employee;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class EmployeeSpecification {

    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "employeeCode", "fullName", "gender", "email", "phone", "address", "startDate", "status",
            "branch.id", "department.id", "position.id"
    );

    public Specification<Employee> build(List<SpecSearchCriteria> criteriaList) {
        criteriaList.forEach(criteria -> {
            if (!ALLOWED_FIELDS.contains(criteria.getKey())) {
                throw new AppException(ErrorCode.INVALID_FIELD_NAME);
            }
        });

        GenericSpecificationBuilder<Employee> builder = new GenericSpecificationBuilder<>();
        criteriaList.forEach(builder::with);
        return builder.build();
    }

}

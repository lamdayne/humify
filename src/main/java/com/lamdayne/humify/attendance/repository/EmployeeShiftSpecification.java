package com.lamdayne.humify.attendance.repository;

import com.lamdayne.humify.attendance.entity.EmployeeShift;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.search.GenericSpecificationBuilder;
import com.lamdayne.humify.common.search.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;

@Component
public class EmployeeShiftSpecification {

    private static final Set<String> ALLOWED_FIELDS = Set.of(
        "employee.id",
        "workShift.id",
        "startDate",
        "endDate"
    );

    public Specification<EmployeeShift> build(List<SpecSearchCriteria> criteriaList, Long companyId) {
        criteriaList.forEach(criteria -> {
            if (!ALLOWED_FIELDS.contains(criteria.getKey())) {
                throw new AppException(ErrorCode.INVALID_FIELD_NAME);
            }
        });

        // Multitenancy filter: employee must belong to the current company
        Specification<EmployeeShift> companySpec = (root, query, cb) -> 
            cb.equal(root.get("employee").get("company").get("id"), companyId);

        GenericSpecificationBuilder<EmployeeShift> builder = new GenericSpecificationBuilder<>();
        criteriaList.forEach(builder::with);

        Specification<EmployeeShift> searchSpec = builder.build();
        return searchSpec != null ? companySpec.and(searchSpec) : companySpec;
    }
}

package com.lamdayne.humify.attendance.repository;

import com.lamdayne.humify.attendance.entity.WorkShift;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.search.GenericSpecificationBuilder;
import com.lamdayne.humify.common.search.SpecSearchCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class WorkShiftSpecification {

    private static final Set<String> ALLOWED_FIELDS = Set.of("shiftCode", "status");

    public Specification<WorkShift> build(List<SpecSearchCriteria> criteriaList) {
        criteriaList.forEach(criteria -> {
            if (!ALLOWED_FIELDS.contains(criteria.getKey())) {
                throw new AppException(ErrorCode.INVALID_FIELD_NAME);
            }
        });

        GenericSpecificationBuilder<WorkShift> builder = new GenericSpecificationBuilder<>();
        criteriaList.forEach(builder::with);
        return builder.build();
    }
}
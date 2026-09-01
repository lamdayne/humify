package com.lamdayne.humify.task.repository;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.search.GenericSpecificationBuilder;
import com.lamdayne.humify.common.search.SpecSearchCriteria;
import com.lamdayne.humify.task.entity.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class TaskSpecification {

    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "type", "priority", "column.category", "assignee.id"
    );

    public Specification<Task> build(List<SpecSearchCriteria> criteriaList) {
        criteriaList.forEach(criteria -> {
            if (!ALLOWED_FIELDS.contains(criteria.getKey())) {
                throw new AppException(ErrorCode.INVALID_FIELD_NAME);
            }
        });

        GenericSpecificationBuilder<Task> builder = new GenericSpecificationBuilder<>();
        criteriaList.forEach(builder::with);
        return builder.build();
    }

}

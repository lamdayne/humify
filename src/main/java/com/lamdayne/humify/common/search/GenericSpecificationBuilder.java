package com.lamdayne.humify.common.search;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static com.lamdayne.humify.common.search.SearchOperation.*;
import static com.lamdayne.humify.common.search.SearchOperation.STARTS_WITH;

public class GenericSpecificationBuilder<T> {

    private final List<SpecSearchCriteria> params;

    public GenericSpecificationBuilder() {
        this.params = new ArrayList<>();
    }

    public GenericSpecificationBuilder<T> with(String key, String operation, String value, String prefix, String suffix) {
        return with(key, operation, value, prefix, suffix, null);
    }

    public GenericSpecificationBuilder<T> with(
            String key, String operation, String value, String prefix, String suffix, String orPredicate
    ) {
        SearchOperation oper = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (oper == SearchOperation.EQUALITY) {
            boolean startWithAsterisk = prefix != null && prefix.contains(ZERO_OR_MORE_REGEX);
            boolean endWithAsterisk = suffix != null && suffix.contains(ZERO_OR_MORE_REGEX);

            if (startWithAsterisk && endWithAsterisk) {
                oper = CONTAINS;
            } else if (startWithAsterisk) {
                oper = ENDS_WITH;
            } else if (endWithAsterisk) {
                oper = STARTS_WITH;
            }
        }
        params.add(new SpecSearchCriteria(key, oper, value, orPredicate));
        return this;
    }

    public GenericSpecificationBuilder<T> with(SpecSearchCriteria criteria) {
        params.add(criteria);
        return this;
    }

    public Specification<T> build() {
        if (params.isEmpty()) return null;

        Specification<T> specification = new GenericSpecification<>(params.get(0));
        for (int i = 1; i < params.size(); i++) {
            SpecSearchCriteria criteria = params.get(i);
            Specification<T> next = new GenericSpecification<>(criteria);
            specification = criteria.isOrPredicate()
                    ? specification.or(next)
                    : specification.and(next);
        }
        return specification;
    }

}

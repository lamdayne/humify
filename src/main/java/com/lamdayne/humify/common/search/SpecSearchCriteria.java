package com.lamdayne.humify.common.search;

import lombok.Getter;

import static com.lamdayne.humify.common.search.SearchOperation.*;

@Getter
public class SpecSearchCriteria {

    private final String key;
    private final SearchOperation operation;
    private final Object value;
    private boolean orPredicate;

    public SpecSearchCriteria(String key, SearchOperation operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public SpecSearchCriteria(String key, SearchOperation operation, Object value, String orPredicate) {
        this.key = key;
        this.operation = operation;
        this.value = value;
        this.orPredicate = orPredicate != null && orPredicate.equals(OR_PREDICATE_FLAG);
    }

    public SpecSearchCriteria(String key, String operation, String value, String suffix, String prefix) {
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
        this.key = key;
        this.operation = oper;
        this.value = value;
    }

    public SpecSearchCriteria(
            String key, String operation, String value, String suffix, String prefix, String orPredicate
    ) {
        this(key, operation, value, suffix, prefix);
        this.orPredicate = orPredicate != null && orPredicate.equals(OR_PREDICATE_FLAG);
    }

}

package com.lamdayne.humify.common.search;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class GenericSpecification<T> implements Specification<T> {

    private SpecSearchCriteria criteria;

    public GenericSpecification(SpecSearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Path<Object> path = resolvePath(root, criteria.getKey());
        Object rawValue = criteria.getValue();

        return switch (criteria.getOperation()) {
            case EQUALITY -> cb.equal(path, FieldValueConverter.convert(path, rawValue));
            case NEGATION -> cb.notEqual(path, FieldValueConverter.convert(path, rawValue));
            case GREATER_THAN ->
                    cb.greaterThan((Path<Comparable>) (Path) path, (Comparable) FieldValueConverter.convert(path, rawValue));
            case LESS_THAN ->
                    cb.lessThan((Path<Comparable>) (Path) path, (Comparable) FieldValueConverter.convert(path, rawValue));
            case LIKE, CONTAINS ->
                    cb.like(cb.lower(path.as(String.class)), String.format("%%%s%%", rawValue.toString().toLowerCase()));
            case STARTS_WITH ->
                    cb.like(cb.lower(path.as(String.class)), String.format("%s%%", rawValue.toString().toLowerCase()));
            case ENDS_WITH ->
                    cb.like(cb.lower(path.as(String.class)), String.format("%%%s", rawValue.toString().toLowerCase()));
        };
    }

    private Path<Object> resolvePath(Root<T> root, String key) {
        String[] parts = key.split("\\.");
        Path<Object> path = root.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            path = path.get(parts[i]);
        }
        return path;
    }

}

package com.lamdayne.humify.common.search;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import jakarta.persistence.criteria.Path;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FieldValueConverter {

    private FieldValueConverter() {}

    @SuppressWarnings("unchecked")
    public static Comparable<Object> convert(Path<?> path, Object rawValue) {
        Class<?> type = path.getJavaType();
        Object converted = convertSingle(type, rawValue.toString());
        return (Comparable<Object>) converted;
    }

    public static Object convertSingle(Class<?> type, String raw) {
        if (type.equals(String.class)) return raw;
        if (type.equals(Integer.class) || type.equals(int.class)) return Integer.parseInt(raw);
        if (type.equals(Long.class) || type.equals(long.class)) return Long.parseLong(raw);
        if (type.equals(Double.class) || type.equals(double.class)) return Double.parseDouble(raw);
        if (type.equals(BigDecimal.class)) return new BigDecimal(raw);
        if (type.equals(Boolean.class) || type.equals(boolean.class)) return Boolean.parseBoolean(raw);
        if (type.equals(LocalDate.class)) return LocalDate.parse(raw);
        if (type.isEnum()) return toEnum(type, raw);
        return raw;
    }

    @SuppressWarnings({"rawtypes"})
    public static Object toEnum(Class type, String raw) {
        Object[] constants = type.getEnumConstants();
        for (Object constant : constants) {
            if (constant.toString().equalsIgnoreCase(raw)) {
                return constant;
            }
        }
        throw new AppException(ErrorCode.INVALID_FILTER_VALUE);
    }

}

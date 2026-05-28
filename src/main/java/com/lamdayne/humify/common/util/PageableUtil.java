package com.lamdayne.humify.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageableUtil {

    private PageableUtil() {
        /* This utility class should not be instantiated */
    }

    /**
     * @param page:  min= 0
     * @param size:  default = 10
     * @param sorts: ex: name:asc, id:desc
     */
    public static Pageable buildPageable(int page, int size, String... sorts) {
        int pageNo = page > 0 ? page - 1 : 0;

        List<Sort.Order> orders = new ArrayList<>();

        if (sorts != null) {
            for (String sort : sorts) {
                Pattern pattern = Pattern.compile("(\\w+?)(:)(asc|desc)");
                Matcher matcher = pattern.matcher(sort);
                if (matcher.find()) {
                    if (matcher.group(3).equalsIgnoreCase("asc")) {
                        orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                    } else {
                        orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                    }
                }
            }
        }

        return PageRequest.of(pageNo, size, orders.isEmpty() ? Sort.unsorted() : Sort.by(orders));
    }

}

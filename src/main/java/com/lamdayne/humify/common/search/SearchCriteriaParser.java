package com.lamdayne.humify.common.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchCriteriaParser {

    private static final Pattern PATTERN = Pattern.compile("(\\w+?)([:<>~!])(\\*?)(.+?)(\\*?)(')?$");

    private SearchCriteriaParser() {

    }

    public static List<SpecSearchCriteria> parse(String[] rawArray) {
        List<SpecSearchCriteria> params = new ArrayList<>();
        for (String raw : rawArray) {
            Matcher matcher = PATTERN.matcher(raw);
            if (matcher.find()) {
                SpecSearchCriteria criteria = new SpecSearchCriteria(
                        matcher.group(1),
                        matcher.group(2),
                        matcher.group(4),
                        matcher.group(3),
                        matcher.group(5),
                        matcher.group(6)
                );
                params.add(criteria);
            }
        }
        return params;
    }

}

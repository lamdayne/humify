package com.lamdayne.humify.common.util;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.sqids.Sqids;

import java.util.List;

@Component
public class SqidsUtil {

    // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789

    @Value("${sqids.min-length}")
    private int minLength;

    @Value("${sqids.keys.company}")
    private String companyKey;

    private Sqids sqids;

    @PostConstruct
    public void init() {
        sqids = Sqids.builder()
                .minLength(minLength)
                .alphabet(companyKey)
                .build();
    }

    public String encode(long id) {
        return sqids.encode(List.of(id));
    }

    public long decode(String publicId) {
        List<Long> numbers = sqids.decode(publicId);
        if (numbers != null && !numbers.isEmpty()) {
            return numbers.get(0);
        }
        throw new AppException(ErrorCode.INVALID_CODE);
    }

}

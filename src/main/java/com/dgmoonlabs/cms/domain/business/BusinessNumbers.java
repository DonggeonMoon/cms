package com.dgmoonlabs.cms.domain.business;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

public final class BusinessNumbers {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();

    private BusinessNumbers() {
    }

    // e.g. O20261102120000-4821; retried while the generated number is already taken
    public static String next(final String prefix, final Predicate<String> isTaken) {
        String number;
        do {
            number = prefix + LocalDateTime.now().format(FORMAT) + "-" + (1000 + RANDOM.nextInt(9000));
        } while (isTaken.test(number));
        return number;
    }
}

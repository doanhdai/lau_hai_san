package com.example.backend_quanlynhahanglau.util;

import java.security.SecureRandom;

public class CodeGenerator {
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate customer code: C-XXXXXX
     */
    public static String generateCustomerCode() {
        int number = random.nextInt(900000) + 100000; // 100000 - 999999
        return "C-" + number;
    }

    /**
     * Generate reservation code: R-XXXXXX
     */
    public static String generateReservationCode() {
        int number = random.nextInt(900000) + 100000;
        return "R-" + number;
    }

    /**
     * Generate order code: O-XXXXXX
     */
    public static String generateOrderCode() {
        int number = random.nextInt(900000) + 100000;
        return "O-" + number;
    }
}

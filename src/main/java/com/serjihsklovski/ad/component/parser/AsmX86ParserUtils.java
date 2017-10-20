package com.serjihsklovski.ad.component.parser;

import java.util.HashSet;
import java.util.Set;

public class AsmX86ParserUtils {

    public static final Set<String> REGISTERS = new HashSet<>();

    private static final String EXCEPTION_MSG_UNSUPPORTED_RADIX = "Unsupported radix!";

    static {
        REGISTERS.add("ax");
        REGISTERS.add("cx");
        REGISTERS.add("dx");
        REGISTERS.add("bx");
    }

    public enum Radix {
        BIN,
        OCT,
        HEX,
        DEC,
    }

    public static boolean isRegister(String lexeme) {
        return REGISTERS.stream()
                .anyMatch(i -> i.equals(lexeme));
    }

    public static long parseNumberFromString(String intValArg) {
        try {
            switch (intValArg.toLowerCase().substring(0, 2)) {
                case "0x":
                    return Long.valueOf(intValArg.substring(2), 16);

                case "0o":
                    return Long.valueOf(intValArg.substring(2), 8);

                case "0b":
                    return Long.valueOf(intValArg.substring(2), 2);

                default:
                    return Long.valueOf(intValArg);
            }
        } catch (IndexOutOfBoundsException ioobe) {
            return Long.valueOf(intValArg);
        }
    }

    public static String encodeNumberToString(long number, Radix radix) {
        switch (radix) {
            case BIN:
                return "0b" + Long.toBinaryString(number);

            case OCT:
                return "0o" + Long.toOctalString(number);

            case DEC:
                return Long.toString(number);

            case HEX:
                return "0x" + Long.toHexString(number);
        }

        throw new RuntimeException(EXCEPTION_MSG_UNSUPPORTED_RADIX);
    }

    public static boolean isValue(String lexeme) {
        try {
            parseNumberFromString(lexeme);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}

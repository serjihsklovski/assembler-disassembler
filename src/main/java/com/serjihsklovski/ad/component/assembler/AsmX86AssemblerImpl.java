package com.serjihsklovski.ad.component.assembler;

import com.serjihsklovski.ad.component.parser.AsmX86ParserUtils;
import com.serjihsklovski.ad.component.OperationTreeNode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class AsmX86AssemblerImpl implements Assembler {

    private static final Map<String, Integer> REGISTER_TO_ORDER_NUMBER = new HashMap<>();

    private static final String EXCEPTION_MSG_UNKNOWN_COMMAND = "An unknown command or argument combination!";

    static {
        REGISTER_TO_ORDER_NUMBER.put("ax", 0);
        REGISTER_TO_ORDER_NUMBER.put("cx", 1);
        REGISTER_TO_ORDER_NUMBER.put("dx", 2);
        REGISTER_TO_ORDER_NUMBER.put("bx", 3);
    }

    @Override
    public String assemble(OperationTreeNode operation) {
        String byteSource = "";

        switch (operation.getValue().toLowerCase()) {
            case "mov":
                if (operation.getChildren().size() == 2) {
                    String argTo = operation.getChildren().get(0).getValue();
                    String argFrom = operation.getChildren().get(1).getValue();

                    if (AsmX86ParserUtils.isRegister(argTo) && AsmX86ParserUtils.isRegister(argFrom)) {
                        byteSource = assembleMovRegReg(argTo, argFrom);
                    } else if (AsmX86ParserUtils.isRegister(argTo) && AsmX86ParserUtils.isValue(argFrom)) {
                        byteSource = assembleMovRegVal(argTo, argFrom);
                    } else {
                        throw new RuntimeException(EXCEPTION_MSG_UNKNOWN_COMMAND);
                    }
                } else {
                    throw new RuntimeException(EXCEPTION_MSG_UNKNOWN_COMMAND);
                }

                break;

            case "add":
                if (operation.getChildren().size() == 2) {
                    String argAcc = operation.getChildren().get(0).getValue();
                    String argFrom = operation.getChildren().get(1).getValue();

                    if (AsmX86ParserUtils.isRegister(argAcc) && AsmX86ParserUtils.isRegister(argFrom)) {
                        byteSource = assembleAddRegReg(argAcc, argFrom);
                    } else if (AsmX86ParserUtils.isRegister(argAcc) && AsmX86ParserUtils.isValue(argFrom)) {
                        byteSource = assembleAddRegVal(argAcc, argFrom);
                    } else {
                        throw new RuntimeException(EXCEPTION_MSG_UNKNOWN_COMMAND);
                    }
                } else {
                    throw new RuntimeException(EXCEPTION_MSG_UNKNOWN_COMMAND);
                }

                break;

            case "not":
                if (operation.getChildren().size() == 1) {
                    String arg = operation.getChildren().get(0).getValue();

                    if (AsmX86ParserUtils.isRegister(arg)) {
                        byteSource = assembleNotReg(arg);
                    } else {
                        throw new RuntimeException(EXCEPTION_MSG_UNKNOWN_COMMAND);
                    }
                } else {
                    throw new RuntimeException(EXCEPTION_MSG_UNKNOWN_COMMAND);
                }

                break;

            case "shr":
                if (operation.getChildren().size() == 1) {
                    String accArg = operation.getChildren().get(0).getValue();

                    if (AsmX86ParserUtils.isRegister(accArg)) {
                        byteSource = assembleShrRegVal(accArg, "1");
                    } else {
                        throw new RuntimeException(EXCEPTION_MSG_UNKNOWN_COMMAND);
                    }
                } else if (operation.getChildren().size() == 2) {
                    String accArg = operation.getChildren().get(0).getValue();
                    String valArg = operation.getChildren().get(1).getValue();

                    if (AsmX86ParserUtils.isRegister(accArg) && AsmX86ParserUtils.isValue(valArg)) {
                        byteSource = assembleShrRegVal(accArg, valArg);
                    } else {
                        throw new RuntimeException(EXCEPTION_MSG_UNKNOWN_COMMAND);
                    }
                } else {
                    throw new RuntimeException(EXCEPTION_MSG_UNKNOWN_COMMAND);
                }

                break;

            case "jmp":
                if (operation.getChildren().size() == 1) {
                    String addressArg = operation.getChildren().get(0).getValue();

                    if (AsmX86ParserUtils.isValue(addressArg)) {
                        byteSource = assembleJmpVal(addressArg);
                    } else {
                        throw new RuntimeException(EXCEPTION_MSG_UNKNOWN_COMMAND);
                    }
                } else {
                    throw new RuntimeException(EXCEPTION_MSG_UNKNOWN_COMMAND);
                }

                break;

            default:
                throw new RuntimeException(EXCEPTION_MSG_UNKNOWN_COMMAND);
        }

        return byteSource;
    }

    private String assembleMovRegReg(String regToArg, String regFromArg) {
        int b = 192 + 8 * REGISTER_TO_ORDER_NUMBER.get(regFromArg) + REGISTER_TO_ORDER_NUMBER.get(regToArg);
        return String.format("6689%s", Integer.toHexString(b));
    }

    private String assembleMovRegVal(String regToArg, String valFromArg) {
        int b1 = 0xb8 + REGISTER_TO_ORDER_NUMBER.get(regToArg);
        int val = (int) AsmX86ParserUtils.parseNumberFromString(valFromArg) % 0x10000;
        int b2 = 0xff & val;
        int b3 = 0xff & (val >> 8);

        String b2Str = Integer.toHexString(b2);
        b2Str = (b2 < 0xf) ? "0" + b2Str : b2Str;

        String b3Str = Integer.toHexString(b3);
        b3Str = (b3 < 0xf) ? "0" + b3Str : b3Str;

        return String.format("66%s%s%s", Integer.toHexString(b1), b2Str, b3Str);
    }

    private String assembleAddRegReg(String regAccArg, String regFromArg) {
        int b = 192 + 8 * REGISTER_TO_ORDER_NUMBER.get(regFromArg) + REGISTER_TO_ORDER_NUMBER.get(regAccArg);
        return String.format("6601%s", Integer.toHexString(b));
    }

    private String assembleAddRegVal(String regToArg, String valFromArg) {
        int val = (int) AsmX86ParserUtils.parseNumberFromString(valFromArg) % 0x10000;

        if ((val >= 0x00) && (val < 0x80)) {
            String valStr = Integer.toHexString(val);
            valStr = (val < 0xf) ? "0" + valStr : valStr;

            return String.format("6683%s%s",
                    Integer.toHexString(0xc0 + REGISTER_TO_ORDER_NUMBER.get(regToArg)),
                    valStr
            );
        } else if ((val >= 0x80) && (val < 0xff80)) {
            int b1 = 0xff & val;
            int b2 = 0xff & (val >> 8);

            String b1Str = Integer.toHexString(b1);
            b1Str = (b1 < 0xf) ? "0" + b1Str : b1Str;

            String b2Str = Integer.toHexString(b2);
            b2Str = (b2 < 0xf) ? "0" + b2Str : b2Str;

            if (regToArg.equals("ax")) {
                return String.format("6605%s%s", b1Str, b2Str);
            } else {
                return String.format("6681%s%s%s",
                        Integer.toHexString(0xc0 + REGISTER_TO_ORDER_NUMBER.get(regToArg)),
                        b1Str,
                        b2Str
                );
            }
        } else if ((val >= 0xff80) && (val < 0x10000)) {
            int b = 0xff & val;

            return String.format("6683%s%s",
                    Integer.toHexString(0xc0 + REGISTER_TO_ORDER_NUMBER.get(regToArg)),
                    Integer.toHexString(b)
            );
        }

        return null;
    }

    private String assembleNotReg(String regArg) {
        return String.format("66f7%s", Integer.toHexString(0xd0 + REGISTER_TO_ORDER_NUMBER.get(regArg)));
    }

    private String assembleShrRegVal(String regAccArg, String intValArg) {
        int val = 0xff & (int) AsmX86ParserUtils.parseNumberFromString(intValArg);

        if (val == 0x1) {
            return String.format("66d1%s", Integer.toHexString(0xe8 + REGISTER_TO_ORDER_NUMBER.get(regAccArg)));
        }

        String valStr = Integer.toHexString(val);
        valStr = (val < 0xf) ? "0" + valStr : valStr;

        return String.format("66c1%s%s",
                Integer.toHexString(0xe8 + REGISTER_TO_ORDER_NUMBER.get(regAccArg)),
                valStr
        );
    }

    private String assembleJmpVal(String addressArg) {
        long address = AsmX86ParserUtils.parseNumberFromString(addressArg) - 4;

        long b1 = 0xffL & address;
        long b2 = 0xffL & (address >> 8);
        long b3 = 0xffL & (address >> 16);
        long b4 = 0xffL & (address >> 24);

        Function<Long, String> toHexString = val -> {
            String str = Long.toHexString(val);
            return (val < 0xfL) ? "0" + str : str;
        };

        String b1Str = toHexString.apply(b1);
        String b2Str = toHexString.apply(b2);
        String b3Str = toHexString.apply(b3);
        String b4Str = toHexString.apply(b4);

        return String.format("e9%s%s%s%s", b1Str, b2Str, b3Str, b4Str);
    }
}

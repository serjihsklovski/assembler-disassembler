package com.serjihsklovski.ad.component.parser;

import com.serjihsklovski.ad.component.OperationTreeNode;

import java.util.*;

public class AsmX86ParserImpl implements Parser {

    private enum Expectation {
        INSTRUCTION,
        REGISTER_AND_REGISTER_OR_NUMBER,
        REGISTER_ONLY,
        REGISTER_AND_UNNECESSARY_NUMBER,
        NUMBER_ONLY,
        REGISTER_OR_NUMBER,
        UNNECESSARY_NUMBER,
        NOTHING,
    }

    private static final Map<String, Expectation> INSTRUCTIONS_TO_EXPECTATIONS = new HashMap<>();

    private static final String EXCEPTION_MSG_UNEXPECTED_TOKEN = "`%s` - an unexpected token! %s";
    private static final String EXCEPTION_MSG_NOT_A_REGISTER = "Not a register.";
    private static final String EXCEPTION_MSG_NOT_A_NUMBER = "Not a number.";
    private static final String EXCEPTION_MSG_NO_MORE_TOKENS = "More tokens are not expected in this instruction, but `%s` was given.";
    private static final String EXCEPTION_MSG_UNSUSPECTED_ERROR = "An unsuspected error occurred when parsing.";

    static {
        INSTRUCTIONS_TO_EXPECTATIONS.put("mov", Expectation.REGISTER_AND_REGISTER_OR_NUMBER);
        INSTRUCTIONS_TO_EXPECTATIONS.put("add", Expectation.REGISTER_AND_REGISTER_OR_NUMBER);
        INSTRUCTIONS_TO_EXPECTATIONS.put("not", Expectation.REGISTER_ONLY);
        INSTRUCTIONS_TO_EXPECTATIONS.put("shr", Expectation.REGISTER_AND_UNNECESSARY_NUMBER);
        INSTRUCTIONS_TO_EXPECTATIONS.put("jmp", Expectation.NUMBER_ONLY);
    }

    private boolean isSupportedInstruction(String lexeme) {
        return INSTRUCTIONS_TO_EXPECTATIONS.keySet().stream()
                .anyMatch(i -> i.equals(lexeme));
    }

    @Override
    public OperationTreeNode parse(List<String> lexemes) {
        OperationTreeNode otn = new OperationTreeNode();
        Expectation expectation = Expectation.INSTRUCTION;

        for (String lexeme : lexemes) {
            if (lexeme.equals(" ") || lexeme.equals("\t")) {
                continue;
            }

            switch (expectation) {
                case INSTRUCTION:
                    if (isSupportedInstruction(lexeme.toLowerCase())) {
                        String l = lexeme.toLowerCase();
                        otn.setValue(l);
                        expectation = INSTRUCTIONS_TO_EXPECTATIONS.get(l);
                    }

                    continue;

                case REGISTER_AND_REGISTER_OR_NUMBER:
                    if (AsmX86ParserUtils.isRegister(lexeme.toLowerCase())) {
                        otn.addChild(new OperationTreeNode(lexeme.toLowerCase()));
                        expectation = Expectation.REGISTER_OR_NUMBER;
                    } else {
                        throw new RuntimeException(
                                String.format(EXCEPTION_MSG_UNEXPECTED_TOKEN, lexeme, EXCEPTION_MSG_NOT_A_REGISTER));
                    }

                    continue;

                case REGISTER_ONLY:
                    if (AsmX86ParserUtils.isRegister(lexeme.toLowerCase())) {
                        otn.addChild(new OperationTreeNode(lexeme.toLowerCase()));
                        expectation = Expectation.NOTHING;
                    } else {
                        throw new RuntimeException(
                                String.format(EXCEPTION_MSG_UNEXPECTED_TOKEN, lexeme, EXCEPTION_MSG_NOT_A_REGISTER));
                    }

                    continue;

                case REGISTER_AND_UNNECESSARY_NUMBER:
                    if (AsmX86ParserUtils.isRegister(lexeme.toLowerCase())) {
                        otn.addChild(new OperationTreeNode(lexeme.toLowerCase()));
                        expectation = Expectation.UNNECESSARY_NUMBER;
                    } else {
                        throw new RuntimeException(
                                String.format(EXCEPTION_MSG_UNEXPECTED_TOKEN, lexeme, EXCEPTION_MSG_NOT_A_REGISTER));
                    }

                    continue;

                case UNNECESSARY_NUMBER:
                    switch (lexeme) {
                        case "\n":
                            expectation = Expectation.NOTHING;
                            continue;

                        case ",":
                            expectation = Expectation.NUMBER_ONLY;
                            continue;

                        default:
                            throw new RuntimeException(
                                    String.format(EXCEPTION_MSG_UNEXPECTED_TOKEN, lexeme, EXCEPTION_MSG_NOT_A_NUMBER));
                    }

                case NUMBER_ONLY:
                    if (AsmX86ParserUtils.isValue(lexeme.toLowerCase())) {
                        otn.addChild(new OperationTreeNode(lexeme.toLowerCase()));
                        expectation = Expectation.NOTHING;
                    } else {
                        throw new RuntimeException(
                                String.format(EXCEPTION_MSG_UNEXPECTED_TOKEN, lexeme, EXCEPTION_MSG_NOT_A_NUMBER));
                    }

                    continue;

                case REGISTER_OR_NUMBER:
                    if (lexeme.equals(",")) {
                        continue;
                    }

                    if (
                            AsmX86ParserUtils.isRegister(lexeme.toLowerCase()) ||
                            AsmX86ParserUtils.isValue(lexeme.toLowerCase())
                    ) {
                        otn.addChild(new OperationTreeNode(lexeme.toLowerCase()));
                        expectation = Expectation.NOTHING;
                    }

                    continue;

                case NOTHING:
                    if (lexeme.equals("\n")) {
                        return otn;
                    }

                    throw new RuntimeException(
                            String.format(EXCEPTION_MSG_UNEXPECTED_TOKEN, EXCEPTION_MSG_NO_MORE_TOKENS, lexeme));
            }
        }

        return otn;
    }

    @Override
    public String parse(OperationTreeNode operation) {
        String cmd = operation.getValue();

        if (operation.getChildren().size() == 1) {
            cmd += " " + operation.getChildren().get(0).getValue();
        } else if (operation.getChildren().size() > 1) {
            cmd += " " + operation.getChildren().stream()
                    .map(OperationTreeNode::getValue)
                    .reduce((v1, v2) -> String.format("%s, %s", v1, v2))
                    .orElseThrow(() -> new RuntimeException(EXCEPTION_MSG_UNSUSPECTED_ERROR));
        }

        return cmd;
    }
}

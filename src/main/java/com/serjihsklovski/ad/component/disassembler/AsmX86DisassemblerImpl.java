package com.serjihsklovski.ad.component.disassembler;

import com.serjihsklovski.ad.component.OperationTreeNode;
import com.serjihsklovski.ad.component.parser.AsmX86ParserUtils;

import java.util.*;

public class AsmX86DisassemblerImpl implements Disassembler {

    private static final Map<Integer, String> ORDER_NUMBER_TO_REGISTER = new HashMap<>();

    private static final String EXCEPTION_MSG_UNSUPPORTED_INSTRUCTION = "An unsupported instruction!";

    private enum DisassemblerState {
        NEW_INSTRUCTION,
        INSTR_66XX,
        INSTR_MOV_REG_REG,
        INSTR_MOV_REG_VAL,
        INSTR_ADD_REG_REG,
        INSTR_ADD_REG_VAL_83,
        INSTR_ADD_REG_VAL_81,
        INSTR_ADD_REG_VAL_05,
        INSTR_NOT,
        INSTR_SHR_D1,
        INSTR_SHR_C1,
        INSTR_JMP,
    }

    static {
        ORDER_NUMBER_TO_REGISTER.put(0, "ax");
        ORDER_NUMBER_TO_REGISTER.put(1, "cx");
        ORDER_NUMBER_TO_REGISTER.put(2, "dx");
        ORDER_NUMBER_TO_REGISTER.put(3, "bx");
    }

    @Override
    public List<OperationTreeNode> disassemble(String byteSource) {
        List<OperationTreeNode> operationTreeNodes = new ArrayList<>();
        List<String> bytes = getBytes(byteSource);

        DisassemblerState state = DisassemblerState.NEW_INSTRUCTION;
        List<String> instruction = new ArrayList<>();

        for (int i = 0; i < bytes.size(); i++) {
            switch (state) {
                case NEW_INSTRUCTION:
                    instruction.clear();
                    instruction.add(bytes.get(i));

                    switch (bytes.get(i)) {
                        case "66":
                            state = DisassemblerState.INSTR_66XX;
                            continue;

                        case "e9":
                            state = DisassemblerState.INSTR_JMP;
                            continue;

                        default:
                            throw new RuntimeException(EXCEPTION_MSG_UNSUPPORTED_INSTRUCTION);
                    }

                case INSTR_66XX:
                    instruction.add(bytes.get(i));

                    switch (bytes.get(i)) {
                        case "89":
                            state = DisassemblerState.INSTR_MOV_REG_REG;
                            continue;

                        case "b8":
                        case "b9":
                        case "ba":
                        case "bb":
                            state = DisassemblerState.INSTR_MOV_REG_VAL;
                            continue;

                        case "01":
                            state = DisassemblerState.INSTR_ADD_REG_REG;
                            continue;

                        case "83":
                            state = DisassemblerState.INSTR_ADD_REG_VAL_83;
                            continue;

                        case "81":
                            state = DisassemblerState.INSTR_ADD_REG_VAL_81;
                            continue;

                        case "05":
                            state = DisassemblerState.INSTR_ADD_REG_VAL_05;
                            continue;

                        case "f7":
                            state = DisassemblerState.INSTR_NOT;
                            continue;

                        case "d1":
                            state = DisassemblerState.INSTR_SHR_D1;
                            continue;

                        case "c1":
                            state = DisassemblerState.INSTR_SHR_C1;
                            continue;

                        default:
                            throw new RuntimeException(EXCEPTION_MSG_UNSUPPORTED_INSTRUCTION);
                    }

                case INSTR_MOV_REG_REG:
                    operationTreeNodes.add(disassembleMovRegReg(bytes.get(i)));
                    state = DisassemblerState.NEW_INSTRUCTION;
                    break;

                case INSTR_MOV_REG_VAL:
                    operationTreeNodes.add(disassembleMovRegVal(instruction.get(1), bytes.get(i), bytes.get(i + 1)));
                    i += 1;
                    state = DisassemblerState.NEW_INSTRUCTION;
                    break;

                case INSTR_ADD_REG_REG:
                    operationTreeNodes.add(disassembleAddRegReg(bytes.get(i)));
                    state = DisassemblerState.NEW_INSTRUCTION;
                    break;

                case INSTR_ADD_REG_VAL_83:
                    operationTreeNodes.add(disassembleAddRegVal83(bytes.get(i), bytes.get(i + 1)));
                    i += 1;
                    state = DisassemblerState.NEW_INSTRUCTION;
                    break;

                case INSTR_ADD_REG_VAL_81:
                    operationTreeNodes.add(disassembleAddRegVal81(bytes.get(i), bytes.get(i + 1), bytes.get(i + 2)));
                    i += 2;
                    state = DisassemblerState.NEW_INSTRUCTION;
                    break;

                case INSTR_ADD_REG_VAL_05:
                    operationTreeNodes.add(disassembleAddRegVal05(bytes.get(i), bytes.get(i + 1)));
                    i += 1;
                    state = DisassemblerState.NEW_INSTRUCTION;
                    break;

                case INSTR_NOT:
                    operationTreeNodes.add(disassembleNot(bytes.get(i)));
                    state = DisassemblerState.NEW_INSTRUCTION;
                    break;

                case INSTR_SHR_D1:
                    operationTreeNodes.add(disassembleShrRegValD1(bytes.get(i)));
                    state = DisassemblerState.NEW_INSTRUCTION;
                    break;

                case INSTR_SHR_C1:
                    operationTreeNodes.add(disassembleShrRegValC1(bytes.get(i), bytes.get(i + 1)));
                    i += 1;
                    state = DisassemblerState.NEW_INSTRUCTION;
                    break;

                case INSTR_JMP:
                    operationTreeNodes.add(disassembleJmp(bytes.get(i), bytes.get(i + 1), bytes.get(i + 2), bytes.get(i + 3)));
                    i += 3;
                    state = DisassemblerState.NEW_INSTRUCTION;
                    break;
            }
        }

        return operationTreeNodes;
    }

    private List<String> getBytes(String byteSource) {
        List<String> bytes = new ArrayList<>();

        for (int i = 0; i < byteSource.length() - 1; i += 2) {
            bytes.add(byteSource.substring(i, i + 2).toLowerCase());
        }

        return bytes;
    }

    private OperationTreeNode disassembleMovRegReg(String registersByteStr) {
        int registersByte = Integer.valueOf(registersByteStr, 16) - 0xc0;
        int r1 = registersByte % 4;
        int r2 = (registersByte - r1) / 8;

        OperationTreeNode otn = new OperationTreeNode("mov");

        otn.addChild(new OperationTreeNode(ORDER_NUMBER_TO_REGISTER.get(r1)));
        otn.addChild(new OperationTreeNode(ORDER_NUMBER_TO_REGISTER.get(r2)));

        return otn;
    }

    private OperationTreeNode disassembleMovRegVal(String registerByte, String b1Str, String b2Str) {
        int register = Integer.valueOf(registerByte, 16) - 0xb8;
        int val = Integer.valueOf(b2Str + b1Str, 16);

        OperationTreeNode otn = new OperationTreeNode("mov");

        otn.addChild(new OperationTreeNode(ORDER_NUMBER_TO_REGISTER.get(register)));
        otn.addChild(new OperationTreeNode(AsmX86ParserUtils.encodeNumberToString(val, AsmX86ParserUtils.Radix.HEX)));

        return otn;
    }

    private OperationTreeNode disassembleAddRegReg(String registersByteStr) {
        int registersByte = Integer.valueOf(registersByteStr, 16) - 0xc0;
        int r1 = registersByte % 4;
        int r2 = (registersByte - r1) / 8;

        OperationTreeNode otn = new OperationTreeNode("add");

        otn.addChild(new OperationTreeNode(ORDER_NUMBER_TO_REGISTER.get(r1)));
        otn.addChild(new OperationTreeNode(ORDER_NUMBER_TO_REGISTER.get(r2)));

        return otn;
    }

    private OperationTreeNode disassembleAddRegVal83(String registerByteStr, String valStr) {
        int registerByte = Integer.valueOf(registerByteStr, 16);
        int val = Integer.valueOf(valStr, 16);

        if (val >= 0x80) {
            val |= 0xff00;
        }

        OperationTreeNode otn = new OperationTreeNode("add");

        otn.addChild(new OperationTreeNode(ORDER_NUMBER_TO_REGISTER.get(registerByte - 0xc0)));
        otn.addChild(new OperationTreeNode(AsmX86ParserUtils.encodeNumberToString(val, AsmX86ParserUtils.Radix.HEX)));

        return otn;
    }

    private OperationTreeNode disassembleAddRegVal81(String registerByteStr, String b1Str, String b2Str) {
        int registerByte = Integer.valueOf(registerByteStr, 16);
        int b1 = Integer.valueOf(b1Str, 16);
        int b2 = Integer.valueOf(b2Str, 16);
        int val = b1 | (b2 << 8);

        OperationTreeNode otn = new OperationTreeNode("add");

        otn.addChild(new OperationTreeNode(ORDER_NUMBER_TO_REGISTER.get(registerByte - 0xc0)));
        otn.addChild(new OperationTreeNode(AsmX86ParserUtils.encodeNumberToString(val, AsmX86ParserUtils.Radix.HEX)));

        return otn;
    }

    private OperationTreeNode disassembleAddRegVal05(String b1Str, String b2Str) {
        int b1 = Integer.valueOf(b1Str, 16);
        int b2 = Integer.valueOf(b2Str, 16);
        int val = b1 | (b2 << 8);

        OperationTreeNode otn = new OperationTreeNode("add");

        otn.addChild(new OperationTreeNode("ax"));
        otn.addChild(new OperationTreeNode(AsmX86ParserUtils.encodeNumberToString(val, AsmX86ParserUtils.Radix.HEX)));

        return otn;
    }

    private OperationTreeNode disassembleNot(String registerByteStr) {
        OperationTreeNode otn = new OperationTreeNode("not");
        otn.addChild(new OperationTreeNode(ORDER_NUMBER_TO_REGISTER.get(Integer.valueOf(registerByteStr, 16) - 0xd0)));
        return otn;
    }

    private OperationTreeNode disassembleShrRegValD1(String registerByteStr) {
        int register = Integer.valueOf(registerByteStr, 16) - 0xe8;

        OperationTreeNode otn = new OperationTreeNode("shr");

        otn.addChild(new OperationTreeNode(ORDER_NUMBER_TO_REGISTER.get(register)));
        otn.addChild(new OperationTreeNode("0x1"));

        return otn;
    }

    private OperationTreeNode disassembleShrRegValC1(String registerByteStr, String valStr) {
        int register = Integer.valueOf(registerByteStr, 16) - 0xe8;
        int val = Integer.valueOf(valStr, 16);

        OperationTreeNode otn = new OperationTreeNode("shr");

        otn.addChild(new OperationTreeNode(ORDER_NUMBER_TO_REGISTER.get(register)));
        otn.addChild(new OperationTreeNode(AsmX86ParserUtils.encodeNumberToString(val, AsmX86ParserUtils.Radix.HEX)));

        return otn;
    }

    private OperationTreeNode disassembleJmp(String b1Str, String b2Str, String b3Str, String b4Str) {
        long val = (Long.valueOf(b4Str + b3Str + b2Str + b1Str, 16) + 4) & 0xffffffffL;
        OperationTreeNode otn = new OperationTreeNode("jmp");
        otn.addChild(new OperationTreeNode(AsmX86ParserUtils.encodeNumberToString(val, AsmX86ParserUtils.Radix.HEX)));
        return otn;
    }
}

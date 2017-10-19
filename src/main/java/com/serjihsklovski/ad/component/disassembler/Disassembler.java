package com.serjihsklovski.ad.component.disassembler;

import com.serjihsklovski.ad.component.OperationTreeNode;

import java.util.List;

public interface Disassembler {

    List<OperationTreeNode> disassemble(String byteSource);
}

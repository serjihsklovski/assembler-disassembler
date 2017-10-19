package com.serjihsklovski.ad.component.parser;

import com.serjihsklovski.ad.component.OperationTreeNode;

import java.util.List;

public interface Parser {

    OperationTreeNode parse(List<String> lexemes);

    String parse(OperationTreeNode operation);
}

package com.serjihsklovski.ad.component;

import java.util.ArrayList;
import java.util.List;

public class OperationTreeNode {

    private OperationTreeNode parent;
    private List<OperationTreeNode> children = new ArrayList<>();
    private String value;

    public OperationTreeNode() {
    }

    public OperationTreeNode(String value) {
        this.value = value;
    }

    public OperationTreeNode(OperationTreeNode parent, String value) {
        this.parent = parent;
        this.value = value;
    }

    public OperationTreeNode getParent() {
        return parent;
    }

    public void setParent(OperationTreeNode parent) {
        this.parent = parent;
    }

    public List<OperationTreeNode> getChildren() {
        return children;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void addChild(OperationTreeNode child) {
        children.add(child);
        child.setParent(this);
    }
}

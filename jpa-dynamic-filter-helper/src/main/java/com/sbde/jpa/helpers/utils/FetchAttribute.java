package com.sbde.jpa.helpers.utils;

import jakarta.persistence.criteria.JoinType;

public class FetchAttribute {
    private String path;
    private JoinType joinType;

    public FetchAttribute(String path) {
        this.path = path;
        this.joinType = JoinType.INNER;
    }

    public FetchAttribute(String path, JoinType joinType) {
        this.path = path;
        this.joinType = joinType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }


}

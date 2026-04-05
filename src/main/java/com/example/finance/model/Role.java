package com.example.finance.model;

public enum Role {
    VIEWER("VIEWER", "Can only view dashboard data"),
    ANALYST("ANALYST", "Can view records and access insights"),
    ADMIN("ADMIN", "Full management access");

    private final String code;
    private final String description;

    Role(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}
package com.example.finance.model;

public enum TransactionCategory {
    SALARY("Salary"),
    BUSINESS("Business Income"),
    INVESTMENT("Investment Returns"),
    RENT("Rent"),
    UTILITIES("Utilities"),
    GROCERIES("Groceries"),
    TRANSPORTATION("Transportation"),
    ENTERTAINMENT("Entertainment"),
    HEALTHCARE("Healthcare"),
    EDUCATION("Education"),
    OTHER("Other");

    private final String displayName;

    TransactionCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }}

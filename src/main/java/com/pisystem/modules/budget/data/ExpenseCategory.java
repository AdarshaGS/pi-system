package com.pisystem.modules.budget.data;

public enum ExpenseCategory {

    FOOD("Food"),
    RENT("Rent"),
    TRANSPORT("Transport"),
    ENTERTAINMENT("Entertainment"),
    SHOPPING("Shopping"),
    UTILITIES("Utilities"),
    HEALTH("Health"),
    EDUCATION("Education"),
    INVESTMENT("Investment"),
    OTHERS("Others"),
    TOTAL("Total"),
    INSURANCE("Insurance"),
    SUBSCRIPTION("Subscription");

    private final String code;

    ExpenseCategory(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public ExpenseCategory fromCode(String code) {
        for (ExpenseCategory category : ExpenseCategory.values()) {
            if (category.getCode().equalsIgnoreCase(code)) {
                return category;
            }
        }
        return ExpenseCategory.OTHERS;
    }
}
package com.common.data;

public enum OnBoardingType {
    MANUAL_ENTRY(1, "Manual Entry"),
    CONNECT_ACCOUNTS(2, "Connect Accounts");

    private final int id;
    private final String name;

    OnBoardingType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static OnBoardingType fromId(int id) {
        for (OnBoardingType type : OnBoardingType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}

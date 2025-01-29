package com.kadirdemirel.ott.enums;

public enum EmailTemplate {
    ONE_TIME_TOKEN_TEMPLATE("one_time_token_template");
    private final String name;

    EmailTemplate(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
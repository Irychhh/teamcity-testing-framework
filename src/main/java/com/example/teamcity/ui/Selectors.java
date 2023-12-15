package com.example.teamcity.ui;

import com.codeborne.selenide.selector.ByAttribute;

public class Selectors {
    public static ByAttribute byId(String value) {
        return new ByAttribute("id", value);
    }

    public static ByAttribute byType(String value) {
        return new ByAttribute("type", value);
    }
}

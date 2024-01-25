package com.example.teamcity.ui.elements;

import com.codeborne.selenide.SelenideElement;
import com.example.teamcity.ui.Selectors;
import lombok.Getter;

@Getter
public class BuildElement extends PageElement {
    private final SelenideElement nameError;
    private final SelenideElement icon;

    public BuildElement(SelenideElement element) {
        super(element);
        this.nameError = findElement(Selectors.byId("errorName"));
        this.icon = findElement("svg");
    }
}

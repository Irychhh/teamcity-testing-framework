package com.example.teamcity.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.element;


public class StartUpPage extends Page{

    private SelenideElement submitButton = element($("input#error_buildTypeName"));
    private SelenideElement acceptLicense = element($("input#accept")); // чекбокс принять лицензию
    private SelenideElement continueButton = element($("input[name=\"Continue\"]")); // кнопка согласия после принятия лицензии
    private SelenideElement restoreFromBackupButton = element($("#nestedPageContent #restoreButton"));
    private SelenideElement proceedButton = element($("#nestedPageContent #proceedButton")); // на двух страницах
    private SelenideElement backFileUploaded = element($(".error#error_buildTypeName"));
    @Getter
    private SelenideElement header = element($("h1#header"));

    public StartUpPage open() {
        Selenide.open("");
        return this;
    }

    public StartUpPage setupTeamCityServer() {
        waitUntilPageLoaded();
        proceedButton.click();
        waitUntilPageLoaded();
        proceedButton.click();
        waitUntilPageLoaded();
        acceptLicense.shouldBe(Condition.enabled, Duration.ofMinutes(5));
        acceptLicense.scrollTo();
        acceptLicense.click();
        continueButton.click();
        return this;
    }
}

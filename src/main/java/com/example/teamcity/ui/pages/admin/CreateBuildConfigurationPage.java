package com.example.teamcity.ui.pages.admin;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.example.teamcity.ui.Selectors;
import com.example.teamcity.ui.pages.Page;

import java.time.Duration;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.element;

public class CreateBuildConfigurationPage extends Page {
    private SelenideElement buildTypeNameInput = element(Selectors.byId("buildTypeName"));
    private SelenideElement urlInput = element(Selectors.byId("url"));
    private SelenideElement nameError = $(".error#error_buildTypeName");

    public CreateBuildConfigurationPage open(String parentProjectId) {
        Selenide.open("/admin/createObjectMenu.html?projectId=" + parentProjectId + "&showMode=createBuildTypeMenu");
        waitUntilPageLoaded();
        return this;
    }
    public CreateBuildConfigurationPage createBuildConfiguration(String url) {
        urlInput.sendKeys(url);
        submit();
        return this;
    }
    public CreateBuildConfigurationPage setupBuild(String buildTypeName) {
        buildTypeNameInput.clear();
        buildTypeNameInput.sendKeys(buildTypeName);
        submit();
        return this;
    }
    public CreateBuildConfigurationPage seeBuildNameError(String expectedErrorMessage) {
        nameError.shouldBe(visible, Duration.ofSeconds(5));
        nameError.shouldHave(text(expectedErrorMessage));
        return this;
    }
}

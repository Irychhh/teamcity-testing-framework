package com.example.teamcity.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.selector.ByAttribute;
import com.example.teamcity.ui.pages.favorites.FavoritesPage;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.element;

public class BuildPage extends FavoritesPage {
    private static final String BUILD_URL = "/buildConfiguration";
    private SelenideElement buildConfiguration = element(new ByAttribute("class", "BuildConfiguration__container--WE"));


    // ElementsCollection -> List <ProjectElement>
    public BuildPage open() {
        Selenide.open(BUILD_URL);
        waitUntilFavoritePageIsLoaded();
        return this;
    }

    public boolean isBuildConfigurationPresent(String buildConfigName) {
        return buildConfiguration.getText().contains(buildConfigName);
    }

    public BuildPage waitForBuildConfiguration(String buildConfigName) {
        buildConfiguration.shouldHave(Condition.text(buildConfigName)).shouldBe(Condition.visible, Duration.ofSeconds(10));
        return this;
    }
}

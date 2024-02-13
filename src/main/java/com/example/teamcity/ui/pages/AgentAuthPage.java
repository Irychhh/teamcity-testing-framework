package com.example.teamcity.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

public class AgentAuthPage extends Page{
    private SelenideElement commentField = $x("//*[@data-test='ring-input']//*[contains(@id, 'ring-input')]");
    private SelenideElement authorizeButton = $x("//button//*[contains(text(), 'Authorize')]");
    private SelenideElement authorizeModalButton = $x("//*[@data-test=\"ring-island-content\"]//button//*[contains(text(), 'Authorize')]");
    private SelenideElement agentLink = $x("//*[contains(@title, 'Agent name:')]");
    @Getter
    private SelenideElement authStatus = $x("//*[@data-agent-authorization-status=\"true\"]//span[1]");

    public AgentAuthPage open() {
        Selenide.open("/agents/unauthorized");
        return this;
    }

    public AgentAuthPage authTeamCityAgent() {
        waitUntilPageLoaded();
        authorizeButton.click();
        waitUntilPageLoaded();
        commentField.shouldBe(Condition.enabled, Duration.ofSeconds(10));
        commentField.sendKeys("test");
        waitUntilPageLoaded();
        authorizeModalButton.click();
        waitUntilPageLoaded();
        agentLink.click();
        authStatus.shouldBe(Condition.enabled, Duration.ofSeconds(10));
        return this;
    }
}

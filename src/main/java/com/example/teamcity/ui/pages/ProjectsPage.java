package com.example.teamcity.ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.selector.ByAttribute;
import com.example.teamcity.ui.elements.ProjectElement;
import com.example.teamcity.ui.pages.favorites.FavoritesPage;

import java.util.List;

import static com.codeborne.selenide.Selenide.elements;

public class ProjectsPage extends FavoritesPage {
    private static final String FAVORITE_PROJECT_URL = "/favorite/projects";
    private ElementsCollection subprojects = elements(new ByAttribute("class", "Subproject__container--WE"));

    // ElementsCollection -> List <ProjectElement>
    public ProjectsPage open() {
        Selenide.open(FAVORITE_PROJECT_URL);
        waitUntilFavoritePageIsLoaded();
        return this;
    }

    public List<ProjectElement> getSubprojects() {
        return generatePageElements(subprojects, ProjectElement::new);
    }
}

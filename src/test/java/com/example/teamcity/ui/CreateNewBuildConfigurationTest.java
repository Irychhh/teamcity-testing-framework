package com.example.teamcity.ui;

import com.example.teamcity.ui.pages.ProjectsPage;
import com.example.teamcity.ui.pages.admin.CreateBuildConfigurationPage;
import com.example.teamcity.ui.pages.admin.CreateNewProject;
import org.testng.annotations.Test;

public class CreateNewBuildConfigurationTest extends BaseUiTest{
    @Test
    public void authorizedUserShouldBeAbleCreateNewBuildConfiguration() {
        var testData = testDataStorage.addTestData();
        var url = "https://github.com/Irychhh/team-city-workshop";

        loginAsUser(testData.getUser());

        new CreateNewProject()
                .open(testData.getProject().getParentProject().getLocator())
                .createProjectByUrl(url)
                .setupProject(testData.getProject().getName(), testData.getBuildType().getName());

        new ProjectsPage().open()
                .getSubprojects()
                .stream().reduce((first, second) -> second).get();

        new CreateBuildConfigurationPage()
                .open(testData.getProject().getParentProject().getLocator())
//                .open(testData.getProject().getId())
                .createBuildConfiguration(url)
                .setupBuild(testData.getBuildType().getName());
    }
    @Test
    public void adminNotShouldBeAbleCreateBuildConfigurationWithEmptyName() {

        var testData = testDataStorage.addTestData();
        var url = "https://github.com/Irychhh/team-city-workshop";
        String ROOT_BUILD_ERROR = "Build configuration name must not be empty";

        loginAsUser(testData.getUser());

        new CreateNewProject()
                .open(testData.getProject().getParentProject().getLocator())
                .createProjectByUrl(url);

        new ProjectsPage().open()
                .getSubprojects()
                .stream().reduce((first, second) -> second).get();

        new CreateBuildConfigurationPage()
                .open(testData.getProject().getParentProject().getLocator())
                .createBuildConfiguration(url)
                .setupBuild("")
                .seeBuildNameError(ROOT_BUILD_ERROR);
    }
}

package com.example.teamcity.api.homework;

import com.example.teamcity.api.BaseApiTest;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.requests.checked.CheckedBuildConfig;
import com.example.teamcity.api.requests.unchecked.UncheckedBuildConfig;
import com.example.teamcity.api.spec.Specifications;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

public class CreateBuildConfigurationTest extends BaseApiTest {
    private final String PERMISSION_ERROR = "You do not have enough permissions to edit project with id: ";

    @Test
    public void systemAdminShouldHaveRightsToCreateBuildConfig() {
        var testData = testDataStorage.addTestData();

        testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.SYSTEM_ADMIN, "g"));

        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        var buildConfig = new CheckedBuildConfig(Specifications.getSpec().authSpec(testData.getUser()))
                .create(testData.getBuildType());

        softy.assertThat(buildConfig.getId()).isEqualTo(testData.getBuildType().getId());
    }

    @Test
    public void projectAdminShouldHaveRightsToCreateBuildConfigInHisProject() {
        var testData = testDataStorage.addTestData();

        checkedWithSuperUser.getProjectRequest()
                .create(testData.getProject());

        testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.PROJECT_ADMIN, "p:" + testData.getProject().getId()));

        checkedWithSuperUser.getUserRequest()
                .create(testData.getUser());

        var buildConfig = new CheckedBuildConfig(Specifications.getSpec().authSpec(testData.getUser()))
                .create(testData.getBuildType());

        softy.assertThat(buildConfig.getId()).isEqualTo(testData.getBuildType().getId());
    }

    @Test
    public void unauthorizedUserShouldNotHaveRightToCreateBuildConfig() {
        var testData = testDataStorage.addTestData();

        new UncheckedBuildConfig(Specifications.getSpec().unauthSpec())
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body(Matchers.equalTo("Authentication required\n" +
                        "To login manually go to \"/login.html\" page"));

        uncheckedWithSuperUser.getBuildConfigRequest()
                .get(testData.getBuildType().getId())
                .then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND)
                .body(Matchers.containsString("No build type nor template is found by id '"
                        + testData.getBuildType().getId() + "'"));
    }

    @Test(enabled = false)
    public void projectAdminShouldNotHaveRightsToCreateBuildConfigToAnotherProject() {
        var firstTestData = testDataStorage.addTestData();
        var secondTestData = testDataStorage.addTestData();

        checkedWithSuperUser.getProjectRequest().create(firstTestData.getProject());
        checkedWithSuperUser.getProjectRequest().create(secondTestData.getProject());

        firstTestData.getUser().setRoles(TestDataGenerator.
                generateRoles(Role.PROJECT_ADMIN, "p:" + firstTestData.getProject().getId()));

        checkedWithSuperUser.getUserRequest().create(firstTestData.getUser());

        secondTestData.getUser().setRoles(TestDataGenerator.
                generateRoles(Role.PROJECT_ADMIN, "p:" + secondTestData.getProject().getId()));

        checkedWithSuperUser.getUserRequest()
                .create(secondTestData.getUser());

        new UncheckedBuildConfig(Specifications.getSpec().authSpec(secondTestData.getUser()))
                .create(firstTestData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void projectAdminShouldNotBeAbleToCreateDuplicateBuildConfig() {
        var testData = testDataStorage.addTestData();

        checkedWithSuperUser.getProjectRequest()
                .create(testData.getProject());
        var createdBuildConfig = checkedWithSuperUser.getBuildConfigRequest()
                .create(testData.getBuildType());

        testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.PROJECT_ADMIN, "p:" + testData.getProject().getId()));
        checkedWithSuperUser.getUserRequest()
                .create(testData.getUser());

        var duplicateBuildConfigRequest = uncheckedWithSuperUser.getBuildConfigRequest().create(testData.getBuildType());

        duplicateBuildConfigRequest
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString(
                        "The build configuration / template ID \""
                                + testData.getBuildType().getId() +
                                "\" is already used by another configuration or template"));

        var updatedBuildConfig = uncheckedWithSuperUser.getBuildConfigRequest()
                .get(createdBuildConfig.getId())
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().as(BuildType.class);

        softy.assertThat(updatedBuildConfig.getId()).isEqualTo(createdBuildConfig.getId());
        softy.assertThat(updatedBuildConfig.getName()).isEqualTo(createdBuildConfig.getName());
    }

    @Test
    public void projectViewerShouldNotHaveRightToCreateBuildConfig() {
        var testData = testDataStorage.addTestData();

        checkedWithSuperUser.getProjectRequest()
                .create(testData.getProject());

        testData.getUser()
                .setRoles(TestDataGenerator.
                        generateRoles(Role.PROJECT_VIEWER, "g"));
        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.containsString(PERMISSION_ERROR + testData.getProject().getId()));
    }

    @Test
    public void projectDeveloperShouldNotHaveRightsToCreateBuildConfig() {
        var testData = testDataStorage.addTestData();

        checkedWithSuperUser.getProjectRequest()
                .create(testData.getProject());

        testData.getUser().
                setRoles(TestDataGenerator.generateRoles(Role.PROJECT_DEVELOPER, "g"));
        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.containsString(PERMISSION_ERROR + testData.getProject().getId()));
    }
}

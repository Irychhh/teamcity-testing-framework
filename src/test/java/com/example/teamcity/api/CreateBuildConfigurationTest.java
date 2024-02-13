package com.example.teamcity.api;

import com.example.teamcity.api.enums.ApiErrorMessages;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.requests.checked.CheckedBuildConfig;
import com.example.teamcity.api.requests.unchecked.UncheckedBuildConfig;
import com.example.teamcity.api.spec.Specifications;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CreateBuildConfigurationTest extends BaseApiTest {
    @DataProvider(name = "createBuildRoleTestData")
    public Object[][] createBuildRoleTestData() {
        return new Object[][]{
                {Role.SYSTEM_ADMIN},
                {Role.PROJECT_ADMIN},
                {Role.AGENT_MANAGER}
        };
    }

    @Test(dataProvider = "createBuildRoleTestData")
    public void rolesWhoShouldHaveRightsToCreateBuildConfig(Role role) {
        var testData = testDataStorage.addTestData();

        testData.getUser().setRoles(TestDataGenerator.generateRoles(role, "g"));

        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

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
                .body(Matchers.containsString(String.format("No build type nor template is found by id '%s'.", testData.getBuildType().getId())));
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
                .body(Matchers.containsString(String.format("The build configuration / template ID \"%s\" is already used by another configuration or template",
                        testData.getBuildType().getId())));

        var updatedBuildConfig = uncheckedWithSuperUser.getBuildConfigRequest()
                .get(createdBuildConfig.getId())
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().as(BuildType.class);

        softy.assertThat(updatedBuildConfig.getId()).isEqualTo(createdBuildConfig.getId());
        softy.assertThat(updatedBuildConfig.getName()).isEqualTo(createdBuildConfig.getName());
    }

    @DataProvider(name = "notCreateBuildRoleTestData")
    public Object[][] notCreateBuildRoleTestData() {
        return new Object[][]{
                {Role.PROJECT_VIEWER},
                {Role.PROJECT_DEVELOPER},
                {Role.TOOLS_INTEGRATION}
        };
    }

    @Test(dataProvider = "notCreateBuildRoleTestData")
    public void rolesWhoShouldNotHaveRightToCreateBuildConfig(Role role) {
        var testData = testDataStorage.addTestData();

        checkedWithSuperUser.getProjectRequest()
                .create(testData.getProject());

        testData.getUser()
                .setRoles(TestDataGenerator.
                        generateRoles(role, "g"));
        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.containsString(ApiErrorMessages.PERMISSION_BUILD_ERROR.getErrorMessage() + testData.getProject().getId()));

        uncheckedWithSuperUser.getProjectRequest().delete(testData.getProject().getId());
        uncheckedWithSuperUser.getUserRequest().delete(testData.getUser().getUsername());
    }

    @Test
    public void sendWithEmptyRequest() {
        var testData = testDataStorage.addTestData();

        testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.SYSTEM_ADMIN, "g"));

        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        uncheckedWithSuperUser.getBuildConfigRequest()
                .create(BuildType
                        .builder()
                        .build())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString(ApiErrorMessages.NOT_FOUND_PROJECT_ERROR.getErrorMessage()));
    }

    @Test
    public void checkErrorForMissingNameRequiredField() {
        var testData = testDataStorage.addTestData();

        testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.SYSTEM_ADMIN, "g"));

        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        uncheckedWithSuperUser.getBuildConfigRequest()
                .create(BuildType
                        .builder()
                        .id(testData.getProject().getId())
                        .project(testData.getProject())
                        .build())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("When creating a build type, non empty name should be provided."));
    }

    @Test
    public void checkErrorForMissingProjectRequiredField() {
        var testData = testDataStorage.addTestData();

        testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.SYSTEM_ADMIN, "g"));

        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        uncheckedWithSuperUser.getBuildConfigRequest()
                .create(BuildType
                        .builder()
                        .id(testData.getProject().getId())
                        .name(testData.getBuildType().getName())
                        .build())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString(ApiErrorMessages.NOT_FOUND_PROJECT_ERROR.getErrorMessage()));
    }

    @Test
    public void checkErrorForRandomRequiredFields() {
        var testData = testDataStorage.addTestData();

        testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.SYSTEM_ADMIN, "g"));

        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        var testId = RandomData.getString();

        testData.getBuildType().getProject().setId(testId);

        uncheckedWithSuperUser.getBuildConfigRequest()
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND)
                .body(Matchers.containsString(String.format("Project cannot be found by external id '%s'.", testId)));
    }

    @Test(enabled = false) // 500 в ответе
    public void checkLengthValidationForId() {
        var testData = testDataStorage.addTestData();

        testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.SYSTEM_ADMIN, "g"));

        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        uncheckedWithSuperUser.getBuildConfigRequest()
                .create(BuildType
                        .builder()
                        .id(RandomData.getCriticalLengthString())
                        .name(RandomData.getCriticalLengthString())
                        .project(testData.getProject())
                        .build())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("ID should start with a latin letter and contain only latin letters, digits and underscores (at most 225 characters)"));
    }
}


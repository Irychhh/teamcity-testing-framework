package com.example.teamcity.api;

import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.NewProjectDescription;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.checked.CheckedBuildConfig;
import com.example.teamcity.api.requests.unchecked.UncheckedBuildConfig;
import com.example.teamcity.api.spec.Specifications;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CreateBuildConfigurationTest extends BaseApiTest {
    private final String PERMISSION_BUILD_ERROR = "You do not have enough permissions to edit project with id: ";
    private final String NOT_FOUND_PROJECT_ERROR = "Build type creation request should contain project node.";

    @DataProvider(name = "createBuildRoleTestData")
    public Object[][] createBuildRoleTestData() {
        return new Object[][]{
                {"SYSTEM_ADMIN"},
                {"PROJECT_ADMIN"},
                {"AGENT_MANAGER"}
        };
    }
    @Test(dataProvider = "createBuildRoleTestData")
    public void rolesWhoShouldHaveRightsToCreateBuildConfig(String role) {
        var testData = testDataStorage.addTestData();

        testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.valueOf(role), "g"));

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

    @DataProvider(name = "notCreateBuildRoleTestData")
    public Object[][] notCreateBuildRoleTestData() {
        return new Object[][]{
                {"PROJECT_VIEWER"},
                {"PROJECT_DEVELOPER"},
                {"TOOLS_INTEGRATION"}
        };
    }
    @Test(dataProvider = "notCreateBuildRoleTestData")
    public void rolesWhoShouldNotHaveRightToCreateBuildConfig(String role) {
        var testData = testDataStorage.addTestData();

        checkedWithSuperUser.getProjectRequest()
                .create(testData.getProject());

        testData.getUser()
                .setRoles(TestDataGenerator.
                        generateRoles(Role.valueOf(role), "g"));
        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.containsString(PERMISSION_BUILD_ERROR + testData.getProject().getId()));

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
                .body(Matchers.containsString(NOT_FOUND_PROJECT_ERROR));
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
                .body(Matchers.containsString(NOT_FOUND_PROJECT_ERROR));
    }

    @Test
    public void checkErrorForRandomRequiredFields() {
        var testData = testDataStorage.addTestData();

        testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.SYSTEM_ADMIN, "g"));

        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        var testId = RandomData.getString();

        uncheckedWithSuperUser.getBuildConfigRequest()
                .create(BuildType
                        .builder()
                        .id(RandomData.getString())
                        .project(NewProjectDescription
                                .builder()
                                .parentProject(Project.builder()
                                        .locator("_Root")
                                        .build())
                                .name(RandomData.getString())
                                .id(testId)
                                .copyAllAssociatedSettings(true)
                                .build())
                        .name(RandomData.getString())
                        .build())
                .then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND)
                .body(Matchers.containsString("Project cannot be found by external id '" + testId + "'."));
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


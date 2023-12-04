package com.example.teamcity.api.homework;

import com.example.teamcity.api.BaseApiTest;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.NewProjectDescription;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.UncheckedRequests;
import com.example.teamcity.api.requests.checked.CheckedProject;
import com.example.teamcity.api.requests.unchecked.UncheckedProject;
import com.example.teamcity.api.spec.Specifications;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

public class CreateProjectTest extends BaseApiTest {
    private final String EMPTY_NAME_ERROR = "Project name cannot be empty.";
    private final String PERMISSION_ERROR = "You do not have \"Create subproject\" permission in project with internal id: _Root";
    private final String PROJECT_SPECIFIED_ERROR = "No project specified. Either 'id', 'internalId' or 'locator' attribute should be present.";

    @Test
    public void systemAdminShouldHaveRightsToCreateProject() {
        var testData = testDataStorage.addTestData();

        testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.SYSTEM_ADMIN, "g"));

        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        var project = new CheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject());

        softy.assertThat(project.getId()).isEqualTo(testData.getProject().getId());
    }

    @Test
    public void projectAdminShouldHaveRightsToCreateProject() {
        var testData = testDataStorage.addTestData();

        testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.PROJECT_ADMIN, "g"));

        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        var project = new CheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject());

        softy.assertThat(project.getId()).isEqualTo(testData.getProject().getId());
    }

    @Test
    public void unauthorizedUserShouldNotHaveRightToCreateProject() {
        var testData = testDataStorage.addTestData();

        new UncheckedRequests(Specifications.getSpec().unauthSpec()).getProjectRequest()
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body(Matchers.equalTo("Authentication required\n" +
                        "To login manually go to \"/login.html\" page"));

        uncheckedWithSuperUser.getProjectRequest()
                .get(testData.getProject().getId())
                .then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND)
                .body(Matchers.containsString("No project found by locator 'count:1,id:"
                        + testData.getProject().getId() + "'"));
    }

    @Test
    public void projectAdminShouldNotBeAbleToCreateDuplicateProject() {
        var testData = testDataStorage.addTestData();

        var createdProject = checkedWithSuperUser.getProjectRequest()
                .create(testData.getProject());

        testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.PROJECT_ADMIN, "p:" + testData.getProject().getId()));
        checkedWithSuperUser.getUserRequest()
                .create(testData.getUser());

        var duplicateProjectRequest = uncheckedWithSuperUser.getProjectRequest().create(testData.getProject());

        duplicateProjectRequest
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project with this name already exists: " + testData.getProject().getName()));

        var updatedProject = uncheckedWithSuperUser.getProjectRequest()
                .get(createdProject.getId())
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().as(Project.class);

        softy.assertThat(updatedProject.getId()).isEqualTo(createdProject.getId());
        softy.assertThat(updatedProject.getName()).isEqualTo(createdProject.getName());
    }

    @Test
    public void checkErrorForMissingNameRequiredField() {
        uncheckedWithSuperUser.getProjectRequest()
                .create(NewProjectDescription
                        .builder()
                        .name(null)
                        .id(RandomData.getString())
                        .copyAllAssociatedSettings(false)
                        .build())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString(EMPTY_NAME_ERROR));

    }

    @Test
    public void checkErrorForMissingLocatorRequiredField() {
        uncheckedWithSuperUser.getProjectRequest()
                .create(NewProjectDescription
                        .builder()
                        .parentProject(Project.builder()
                                .locator(null)
                                .build())
                        .name(RandomData.getString())
                        .id(RandomData.getString())
                        .copyAllAssociatedSettings(true)
                        .build())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString(PROJECT_SPECIFIED_ERROR));
    }

    @Test(enabled = false) // 500 в ответе
    public void checkLengthValidationForNameAndId() {
        uncheckedWithSuperUser.getProjectRequest()
                .create(NewProjectDescription
                        .builder()
                        .name(RandomData.getCriticalLengthString())
                        .id(RandomData.getCriticalLengthString())
                        .copyAllAssociatedSettings(true)
                        .build())
                .then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND
                )
                .body(Matchers.containsString(PROJECT_SPECIFIED_ERROR));
    }

    @Test
    public void checkLengthValidationForLocator() {
        uncheckedWithSuperUser.getProjectRequest()
                .create(NewProjectDescription
                        .builder()
                        .parentProject(Project.builder()
                                .locator(RandomData.getCriticalLengthString())
                                .build())
                        .name(RandomData.getString())
                        .id(RandomData.getString())
                        .copyAllAssociatedSettings(true)
                        .build())
                .then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND)
                .body(Matchers.containsString("No project found by name or internal/external id"));
    }

    @Test
    public void sendWithEmptyRequest() {
        uncheckedWithSuperUser.getProjectRequest()
                .create(NewProjectDescription
                        .builder()
                        .build())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString(EMPTY_NAME_ERROR));
    }

    @Test
    public void projectViewerShouldNotHaveRightToCreateProject() {
        var testData = testDataStorage.addTestData();
        testData.getUser()
                .setRoles(TestDataGenerator.
                generateRoles(Role.PROJECT_VIEWER, "g"));
        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.containsString(PERMISSION_ERROR));
    }

    @Test
    public void projectDeveloperShouldNotHaveRightsToCreateProject() {
        var testData = testDataStorage.addTestData();
        testData.getUser().
                setRoles(TestDataGenerator.generateRoles(Role.PROJECT_DEVELOPER, "g"));
        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.containsString(PERMISSION_ERROR));
    }
}
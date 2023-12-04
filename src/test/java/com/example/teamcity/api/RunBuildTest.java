package com.example.teamcity.api;

import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.requests.unchecked.UncheckedRunBuild;
import com.example.teamcity.api.spec.Specifications;
import org.apache.hc.core5.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;

public class RunBuildTest extends BaseApiTest {
    @Test
    public void systemAdminShouldBeAbleToRunBuildConfiguration() {
        var testData = testDataStorage.addTestData();

        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.SYSTEM_ADMIN, "g"));
        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        var buildConfig = checkedWithSuperUser.getBuildConfigRequest().create(testData.getBuildType());

        var runBuildConfig = checkedWithSuperUser.getRunBuildRequest().create(testData.getRunBuild());

        softy.assertThat(buildConfig.getId()).isNotNull();
        softy.assertThat(runBuildConfig.getId()).isNotNull();
        softy.assertThat(buildConfig.getProject().getId()).isEqualTo(testData.getProject().getId());
        softy.assertThat(runBuildConfig.getBuildTypeId()).isEqualTo(testData.getBuildType().getId());
        softy.assertThat(runBuildConfig.getBuildType().getId()).isEqualTo(testData.getBuildType().getId());
    }

    @Test
    public void unauthorizedUserShouldNotHaveRightToRunBuildConfiguration() {
        var testData = testDataStorage.addTestData();

        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        checkedWithSuperUser.getBuildConfigRequest().create(testData.getBuildType());

        new UncheckedRunBuild(Specifications.getSpec().unauthSpec())
                .create(testData.getRunBuild())
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body(equalTo("Authentication required\n" +
                        "To login manually go to \"/login.html\" page"));

        uncheckedWithSuperUser.getRunBuildRequest()
                .get(testData.getRunBuild().getId())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Invalid value of dimension 'id': '"
                        + testData.getRunBuild().getId() + "'. Should be a number."));

        uncheckedWithSuperUser.getBuildConfigRequest()
                .get(testData.getBuildType().getId())
                .then().assertThat().statusCode(org.apache.http.HttpStatus.SC_OK)
                .body("triggers.count", equalTo(0));
    }
}


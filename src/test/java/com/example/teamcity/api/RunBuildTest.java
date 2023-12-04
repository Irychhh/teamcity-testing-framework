package com.example.teamcity.api;

import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.TestDataGenerator;
import org.testng.annotations.Test;

public class RunBuildTest extends BaseApiTest{
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
}

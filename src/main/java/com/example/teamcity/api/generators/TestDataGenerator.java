package com.example.teamcity.api.generators;

import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.NewBuildTypeId;
import com.example.teamcity.api.models.NewProjectDescription;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.Role;
import com.example.teamcity.api.models.Roles;
import com.example.teamcity.api.models.RunBuild;
import com.example.teamcity.api.models.User;

import java.util.Arrays;


public class TestDataGenerator {
    public static TestData generate() {
        var user = User.builder()
                .username(RandomData.getString())
                .password(RandomData.getString())
                .email(RandomData.getString() + "@gmail.com")
                .roles(Roles.builder()
                        .role(Arrays.asList(Role.builder()
                                .roleId("SYSTEM_ADMIN")
                                .scope("g")
                                .build()))
                        .build())
                .build();
        var project = NewProjectDescription
                .builder()
                .parentProject(Project.builder()
                        .locator("_Root")
                        .build())
                .name(RandomData.getString())
                .id(RandomData.getString())
                .copyAllAssociatedSettings(true)
                .build();

        var buildType = BuildType.builder()
                .id(RandomData.getString())
                .name(RandomData.getString())
                .project(project)
                .build();

        var buildTypeObject = NewBuildTypeId.builder()
                .id(buildType.getId())
                .build();

        var runBuild = RunBuild.builder()
                .buildType(buildTypeObject)
                .build();

        return TestData.builder()
                .user(user)
                .project(project)
                .buildType(buildType)
                .runBuild(runBuild)
                .build();
    }

    public static Roles generateRoles(com.example.teamcity.api.enums.Role role, String scope) {
        return Roles.builder()
                .role(Arrays.asList(Role.builder()
                        .roleId(role.getText())
                        .scope(scope)
                        .build()))
                .build();

    }
}

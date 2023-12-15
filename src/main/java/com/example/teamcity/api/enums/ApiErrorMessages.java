package com.example.teamcity.api.enums;

public enum ApiErrorMessages {
    EMPTY_NAME_ERROR("Project name cannot be empty."),
    PERMISSION_ERROR("You do not have \"Create subproject\" permission in project with internal id: _Root"),
    PROJECT_SPECIFIED_ERROR("No project specified. Either 'id', 'internalId' or 'locator' attribute should be present."),
    PERMISSION_BUILD_ERROR("You do not have enough permissions to edit project with id: "),
    NOT_FOUND_PROJECT_ERROR("Build type creation request should contain project node.");

    private final String errorMessage;

    ApiErrorMessages(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

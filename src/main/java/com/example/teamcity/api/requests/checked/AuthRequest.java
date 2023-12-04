package com.example.teamcity.api.requests.checked;

import com.example.teamcity.api.models.User;
import com.example.teamcity.api.spec.Specifications;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

public class AuthRequest {
    private final RequestSpecification user;
    public AuthRequest(RequestSpecification spec) {
        this.user = spec;
    }

    public String getCsrfToken() {
        return RestAssured
                .given()
                .spec(Specifications.getSpec().authSpec((User) user))
                .get("/authenticationTest.html?crsf")
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().asString();
    }
}

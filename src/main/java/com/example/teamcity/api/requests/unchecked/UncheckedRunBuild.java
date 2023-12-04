package com.example.teamcity.api.requests.unchecked;

import com.example.teamcity.api.requests.CrudInterface;
import com.example.teamcity.api.requests.Request;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class UncheckedRunBuild extends Request implements CrudInterface {
    private static final String RUN_BUILD_ENDPOINT = "/app/rest/buildQueue";

    public UncheckedRunBuild(RequestSpecification spec) {
        super(spec);
    }


    @Override
    public Response create(Object obj) {
        return given()
                .spec(spec)
                .body(obj)
                .post(RUN_BUILD_ENDPOINT);
    }

    @Override
    public Object get(String id) {
        return null;
    }

    @Override
    public Object update(String id, Object obj) {
        return null;
    }

    @Override
    public Response delete(String id) {
        return given()
                .spec(spec)
                .delete(RUN_BUILD_ENDPOINT + "/id:" + id);
    }
}

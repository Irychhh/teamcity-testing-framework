package com.example.teamcity.api.requests.checked;

import com.example.teamcity.api.models.RunBuild;
import com.example.teamcity.api.requests.CrudInterface;
import com.example.teamcity.api.requests.Request;
import com.example.teamcity.api.requests.unchecked.UncheckedRunBuild;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

public class CheckedRunBuild extends Request implements CrudInterface {
    public CheckedRunBuild(RequestSpecification spec) {
        super(spec);
    }

    @Override
    public RunBuild create(Object obj) {
        return new UncheckedRunBuild(spec)
                .create(obj)
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().as(RunBuild.class);
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
    public Object delete(String id) {
        return new UncheckedRunBuild(spec)
                .delete(id)
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().asString();
    }
}

package net.laprun.sustainability.power.quarkus.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class PowerResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/power")
                .then()
                .statusCode(200)
                .body(is("Hello power"));
    }
}

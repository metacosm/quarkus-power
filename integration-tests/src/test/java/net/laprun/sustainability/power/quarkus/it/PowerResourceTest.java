package net.laprun.sustainability.power.quarkus.it;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

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

package com.lolmeida.peahdb.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@DisplayName("GreetingResource")
class GreetingResourceTest {

    @Nested
    class HelloEndpointTest {
        @Test
        @DisplayName("Should return greeting message")
        void shouldReturnGreetingMessage() {
            given()
                    .when().get("/hello")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.TEXT)
                    .body(is("Hello from Quarkus REST"));
        }

        @Test
        @DisplayName("Should handle GET request correctly")
        void shouldHandleGetRequestCorrectly() {
            given()
                    .when().get("/hello")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.TEXT);
        }

        @Test
        @DisplayName("Should return consistent response on multiple calls")
        void shouldReturnConsistentResponseOnMultipleCalls() {
            String expectedResponse = "Hello from Quarkus REST";

            // First call
            given()
                    .when().get("/hello")
                    .then()
                    .statusCode(200)
                    .body(is(expectedResponse));

            // Second call
            given()
                    .when().get("/hello")
                    .then()
                    .statusCode(200)
                    .body(is(expectedResponse));

            // Third call
            given()
                    .when().get("/hello")
                    .then()
                    .statusCode(200)
                    .body(is(expectedResponse));
        }
    }

    @Nested
    class InvalidEndpointTest {
        @Test
        @DisplayName("Should return 404 for invalid endpoint")
        void shouldReturn404ForInvalidEndpoint() {
            given()
                    .when().get("/hello/invalid")
                    .then()
                    .statusCode(404);
        }

        @Test
        @DisplayName("Should return 405 for POST request")
        void shouldReturn405ForPostRequest() {
            given()
                    .when().post("/hello")
                    .then()
                    .statusCode(405);
        }

        @Test
        @DisplayName("Should return 405 for PUT request")
        void shouldReturn405ForPutRequest() {
            given()
                    .when().put("/hello")
                    .then()
                    .statusCode(405);
        }

        @Test
        @DisplayName("Should return 405 for DELETE request")
        void shouldReturn405ForDeleteRequest() {
            given()
                    .when().delete("/hello")
                    .then()
                    .statusCode(405);
        }
    }
}

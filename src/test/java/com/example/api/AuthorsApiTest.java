package com.example.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Test suite for the Authors API endpoints.
 * Includes happy path and edge case scenarios.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthorsApiTest extends BaseTest {
    private static int createdAuthorId;

    /**
     * Verify that retrieving all authors returns status 200 and JSON content type.
     */
    @Test
    @Order(1)
    public void getAllAuthors() {
        given()
            .when()
            .get("/api/v1/Authors")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    /**
     * Create a new author and capture its ID for later tests.
     */
    @Test
    @Order(2)
    public void createAuthor() {
        String newAuthor = """
        {
            "firstName": "Test",
            "lastName": "Author"
        }
        """;
        Response response = given()
            .contentType(ContentType.JSON)
            .body(newAuthor)
            .when()
            .post("/api/v1/Authors")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();
        createdAuthorId = response.jsonPath().getInt("id");
        Assertions.assertTrue(createdAuthorId > 0, "Author ID should be greater than zero");
    }

    /**
     * Retrieve the author by ID and verify the returned ID matches the created author ID.
     */
    @Test
    @Order(3)
    public void getAuthorById() {
        Assumptions.assumeTrue(createdAuthorId > 0, "Author ID not set from creation test");
        given()
            .pathParam("id", createdAuthorId)
            .when()
            .get("/api/v1/Authors/{id}")
            .then()
            .statusCode(200)
            .body("id", equalTo(createdAuthorId));
    }

    /**
     * Update the existing author and verify a success status code.
     */
    @Test
    @Order(4)
    public void updateAuthor() {
        Assumptions.assumeTrue(createdAuthorId > 0, "Author ID not set from creation test");
        String updatedAuthor = """
        {
            "id": %d,
            "firstName": "Updated",
            "lastName": "Author"
        }
        """.formatted(createdAuthorId);
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", createdAuthorId)
            .body(updatedAuthor)
            .when()
            .put("/api/v1/Authors/{id}")
            .then()
            .statusCode(anyOf(is(200), is(204)));
    }

    /**
     * Delete the created author and verify a success status code.
     */
    @Test
    @Order(5)
    public void deleteAuthor() {
        Assumptions.assumeTrue(createdAuthorId > 0, "Author ID not set from creation test");
        given()
            .pathParam("id", createdAuthorId)
            .when()
            .delete("/api/v1/Authors/{id}")
            .then()
            .statusCode(anyOf(is(200), is(204)));
    }

    /**
     * Attempt to retrieve an author with a non-existent ID and expect a client error status code.
     */
    @Test
    public void getAuthorWithInvalidId() {
        given()
            .pathParam("id", 9999999)
            .when()
            .get("/api/v1/Authors/{id}")
            .then()
            .statusCode(anyOf(is(400), is(404)));
    }

    /**
     * Attempt to create an author without the required firstName field; expect a client error.
     */
    @Test
    public void createAuthorMissingFirstName() {
        String invalidAuthor = """
        {
            "lastName": "MissingFirstName"
        }
        """;
        given()
            .contentType(ContentType.JSON)
            .body(invalidAuthor)
            .when()
            .post("/api/v1/Authors")
            .then()
            .statusCode(anyOf(is(400), is(422), is(404)));
    }
}

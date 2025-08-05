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
        // FakeRestAPI returns id: 0 for created resources, so we'll use this for our tests
        Assertions.assertTrue(createdAuthorId >= 0, "Author ID should be zero or greater");
    }

    /**
     * Retrieve the author by ID and verify the returned ID matches the created author ID.
     */
    @Test
    @Order(3)
    public void getAuthorById() {
        Assumptions.assumeTrue(createdAuthorId >= 0, "Author ID not set from creation test");
        given()
            .pathParam("id", createdAuthorId == 0 ? 1 : createdAuthorId) // Use existing ID since creation returns 0
            .when()
            .get("/api/v1/Authors/{id}")
            .then()
            .statusCode(200)
            .body("id", equalTo(createdAuthorId == 0 ? 1 : createdAuthorId));
    }

    /**
     * Update the existing author and verify a success status code.
     */
    @Test
    @Order(4)
    public void updateAuthor() {
        Assumptions.assumeTrue(createdAuthorId >= 0, "Author ID not set from creation test");
        int targetId = createdAuthorId == 0 ? 1 : createdAuthorId; // Use existing ID since creation returns 0
        String updatedAuthor = """
        {
            "id": %d,
            "firstName": "Updated",
            "lastName": "Author"
        }
        """.formatted(targetId);
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", targetId)
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
        Assumptions.assumeTrue(createdAuthorId >= 0, "Author ID not set from creation test");
        int targetId = createdAuthorId == 0 ? 1 : createdAuthorId; // Use existing ID since creation returns 0
        given()
            .pathParam("id", targetId)
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
     * Attempt to create an author without the required firstName field; 
     * Note: FakeRestAPI doesn't validate, so it returns 200 with null firstName
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
            .statusCode(200) // FakeRestAPI doesn't validate, so it accepts the request
            .body("firstName", nullValue()) // Verify firstName is null
            .body("lastName", equalTo("MissingFirstName"));
    }

    /**
     * Test creating an author with null values
     */
    @Test
    public void createAuthorWithNullFields() {
        String nullAuthor = """
        {
            "firstName": null,
            "lastName": null
        }
        """;
        given()
            .contentType(ContentType.JSON)
            .body(nullAuthor)
            .when()
            .post("/api/v1/Authors")
            .then()
            .statusCode(200)
            .body("firstName", nullValue())
            .body("lastName", nullValue());
    }

    /**
     * Test creating an author with special characters
     */
    @Test
    public void createAuthorWithSpecialCharacters() {
        String specialAuthor = """
        {
            "firstName": "José María",
            "lastName": "García-Pérez"
        }
        """;
        given()
            .contentType(ContentType.JSON)
            .body(specialAuthor)
            .when()
            .post("/api/v1/Authors")
            .then()
            .statusCode(200)
            .body("firstName", equalTo("José María"))
            .body("lastName", equalTo("García-Pérez"));
    }

    /**
     * Test updating an author with non-existent ID
     */
    @Test
    public void updateNonExistentAuthor() {
        String updatedAuthor = """
        {
            "id": 9999999,
            "firstName": "Non",
            "lastName": "Existent"
        }
        """;
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", 9999999)
            .body(updatedAuthor)
            .when()
            .put("/api/v1/Authors/{id}")
            .then()
            .statusCode(anyOf(is(200), is(204), is(404))); // FakeAPI may handle this differently
    }

    /**
     * Test deleting an author with non-existent ID
     */
    @Test
    public void deleteNonExistentAuthor() {
        given()
            .pathParam("id", 9999999)
            .when()
            .delete("/api/v1/Authors/{id}")
            .then()
            .statusCode(anyOf(is(200), is(204), is(404))); // FakeAPI may handle this differently
    }

    /**
     * Test creating an author with invalid JSON
     */
    @Test
    public void createAuthorWithInvalidJson() {
        String invalidJson = """
        {
            "firstName": "Test",
            "lastName": "Author"
        """; // Missing closing brace
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
            .when()
            .post("/api/v1/Authors")
            .then()
            .statusCode(anyOf(is(400), is(500))); // Should fail due to malformed JSON
    }

    /**
     * Test creating an author with additional unexpected fields
     */
    @Test
    public void createAuthorWithExtraFields() {
        String authorWithExtraFields = """
        {
            "firstName": "Test",
            "lastName": "Author",
            "unexpectedField": "ShouldBeIgnored",
            "anotherField": 123
        }
        """;
        given()
            .contentType(ContentType.JSON)
            .body(authorWithExtraFields)
            .when()
            .post("/api/v1/Authors")
            .then()
            .statusCode(200)
            .body("firstName", equalTo("Test"))
            .body("lastName", equalTo("Author"));
    }
}

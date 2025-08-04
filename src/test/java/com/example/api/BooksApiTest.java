package com.example.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Test suite for the Books API endpoints.
 * Covers happy paths as well as common edge cases.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BooksApiTest extends BaseTest {
    private static int createdBookId;

    /**
     * Verify that retrieving the list of all books returns a 200 status and JSON content type.
     */
    @Test
    @Order(1)
    public void getAllBooks() {
        given()
            .when()
            .get("/api/v1/Books")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    /**
     * Create a new book and capture its ID for subsequent tests.
     */
    @Test
    @Order(2)
    public void createBook() {
        String newBookJson = """
        {
            "title": "Automated Test Book",
            "description": "Book created by API automation tests",
            "pageCount": 123,
            "excerpt": "Testing is fun!",
            "publishDate": "2020-01-01T00:00:00"
        }
        """;
        Response response = given()
            .contentType(ContentType.JSON)
            .body(newBookJson)
            .when()
            .post("/api/v1/Books")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .extract().response();
        createdBookId = response.jsonPath().getInt("id");
        Assertions.assertTrue(createdBookId > 0, "Created book ID should be greater than zero");
    }

    /**
     * Retrieve the newly created book by its ID and verify the ID matches.
     */
    @Test
    @Order(3)
    public void getBookById() {
        Assumptions.assumeTrue(createdBookId > 0, "Book ID not set from creation test");
        given()
            .pathParam("id", createdBookId)
            .when()
            .get("/api/v1/Books/{id}")
            .then()
            .statusCode(200)
            .body("id", equalTo(createdBookId));
    }

    /**
     * Update the existing book and verify a successful status code.
     */
    @Test
    @Order(4)
    public void updateBook() {
        Assumptions.assumeTrue(createdBookId > 0, "Book ID not set from creation test");
        String updatedBookJson = """
        {
            "id": %d,
            "title": "Updated Test Book",
            "description": "Updated description",
            "pageCount": 456,
            "excerpt": "Updated excerpt",
            "publishDate": "2021-01-01T00:00:00"
        }
        """.formatted(createdBookId);
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", createdBookId)
            .body(updatedBookJson)
            .when()
            .put("/api/v1/Books/{id}")
            .then()
            .statusCode(anyOf(is(200), is(204)));
    }

    /**
     * Delete the book and verify a successful status code.
     */
    @Test
    @Order(5)
    public void deleteBook() {
        Assumptions.assumeTrue(createdBookId > 0, "Book ID not set from creation test");
        given()
            .pathParam("id", createdBookId)
            .when()
            .delete("/api/v1/Books/{id}")
            .then()
            .statusCode(anyOf(is(200), is(204)));
    }

    /**
     * Attempt to retrieve a book with a non-existent ID and expect a client error status code.
     */
    @Test
    public void getBookWithInvalidId() {
        given()
            .pathParam("id", 9999999)
            .when()
            .get("/api/v1/Books/{id}")
            .then()
            .statusCode(anyOf(is(400), is(404)));
    }

    /**
     * Attempt to create a book without a required title field; expect a client error.
     */
    @Test
    public void createBookWithoutTitle() {
        String incompleteBook = """
        {
            "description": "Missing title field",
            "pageCount": 100,
            "excerpt": "No title",
            "publishDate": "2020-01-01T00:00:00"
        }
        """;
        given()
            .contentType(ContentType.JSON)
            .body(incompleteBook)
            .when()
            .post("/api/v1/Books")
            .then()
            .statusCode(anyOf(is(400), is(422), is(404)));
    }
}

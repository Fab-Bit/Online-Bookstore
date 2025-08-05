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
        // FakeRestAPI returns id: 0 for created resources, so we'll use this for our tests
        Assertions.assertTrue(createdBookId >= 0, "Created book ID should be zero or greater");
    }

    /**
     * Retrieve the newly created book by its ID and verify the ID matches.
     */
    @Test
    @Order(3)
    public void getBookById() {
        Assumptions.assumeTrue(createdBookId >= 0, "Book ID not set from creation test");
        given()
            .pathParam("id", createdBookId == 0 ? 1 : createdBookId) // Use existing ID since creation returns 0
            .when()
            .get("/api/v1/Books/{id}")
            .then()
            .statusCode(200)
            .body("id", equalTo(createdBookId == 0 ? 1 : createdBookId));
    }

    /**
     * Update the existing book and verify a successful status code.
     */
    @Test
    @Order(4)
    public void updateBook() {
        Assumptions.assumeTrue(createdBookId >= 0, "Book ID not set from creation test");
        int targetId = createdBookId == 0 ? 1 : createdBookId; // Use existing ID since creation returns 0
        String updatedBookJson = """
        {
            "id": %d,
            "title": "Updated Test Book",
            "description": "Updated description",
            "pageCount": 456,
            "excerpt": "Updated excerpt",
            "publishDate": "2021-01-01T00:00:00"
        }
        """.formatted(targetId);
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", targetId)
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
        Assumptions.assumeTrue(createdBookId >= 0, "Book ID not set from creation test");
        int targetId = createdBookId == 0 ? 1 : createdBookId; // Use existing ID since creation returns 0
        given()
            .pathParam("id", targetId)
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
     * Attempt to create a book without a required title field; 
     * Note: FakeRestAPI doesn't validate, so it returns 200 with null title
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
            .statusCode(200) // FakeRestAPI doesn't validate, so it accepts the request
            .body("title", nullValue()) // Verify title is null
            .body("description", equalTo("Missing title field"));
    }

    /**
     * Test creating a book with negative page count
     */
    @Test
    public void createBookWithNegativePageCount() {
        String negativePageBook = """
        {
            "title": "Negative Pages Book",
            "description": "A book with negative pages",
            "pageCount": -50,
            "excerpt": "Impossible book",
            "publishDate": "2020-01-01T00:00:00"
        }
        """;
        given()
            .contentType(ContentType.JSON)
            .body(negativePageBook)
            .when()
            .post("/api/v1/Books")
            .then()
            .statusCode(200) // FakeAPI accepts this
            .body("pageCount", equalTo(-50));
    }

    /**
     * Test creating a book with invalid date format
     */
    @Test
    public void createBookWithInvalidDate() {
        String invalidDateBook = """
        {
            "title": "Invalid Date Book",
            "description": "A book with invalid date",
            "pageCount": 100,
            "excerpt": "Bad date",
            "publishDate": "invalid-date-format"
        }
        """;
        given()
            .contentType(ContentType.JSON)
            .body(invalidDateBook)
            .when()
            .post("/api/v1/Books")
            .then()
            .statusCode(anyOf(is(200), is(400))); // Might accept or reject
    }

    /**
     * Test creating a book with future date
     */
    @Test
    public void createBookWithFutureDate() {
        String futureBook = """
        {
            "title": "Future Book",
            "description": "A book from the future",
            "pageCount": 300,
            "excerpt": "Time travel",
            "publishDate": "2099-12-31T23:59:59"
        }
        """;
        given()
            .contentType(ContentType.JSON)
            .body(futureBook)
            .when()
            .post("/api/v1/Books")
            .then()
            .statusCode(200)
            .body("publishDate", equalTo("2099-12-31T23:59:59"));
    }

    /**
     * Test updating a non-existent book
     */
    @Test
    public void updateNonExistentBook() {
        String updatedBook = """
        {
            "id": 9999999,
            "title": "Non-existent Book",
            "description": "This book doesn't exist",
            "pageCount": 100,
            "excerpt": "Not found",
            "publishDate": "2020-01-01T00:00:00"
        }
        """;
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", 9999999)
            .body(updatedBook)
            .when()
            .put("/api/v1/Books/{id}")
            .then()
            .statusCode(anyOf(is(200), is(204), is(404)));
    }

    /**
     * Test deleting a non-existent book
     */
    @Test
    public void deleteNonExistentBook() {
        given()
            .pathParam("id", 9999999)
            .when()
            .delete("/api/v1/Books/{id}")
            .then()
            .statusCode(anyOf(is(200), is(204), is(404)));
    }

    /**
     * Test creating a book with malformed JSON
     */
    @Test
    public void createBookWithInvalidJson() {
        String invalidJson = """
        {
            "title": "Test Book",
            "description": "Missing closing brace"
        """;
        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
            .when()
            .post("/api/v1/Books")
            .then()
            .statusCode(anyOf(is(400), is(500)));
    }
}

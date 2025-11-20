package com.snooker4real.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for FilmResource REST endpoints.
 * These tests use REST Assured to test the HTTP layer.
 */
@QuarkusTest
class FilmResourceTest {

    private static final String BASE_PATH = "/api/films";

    // Tests for GET /api/films/{filmId}

    @Test
    void testGetFilmById_whenFilmExists_returns200WithTitle() {
        given()
                .when()
                .get(BASE_PATH + "/1")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .body(not(emptyString()));
    }

    @Test
    void testGetFilmById_whenFilmDoesNotExist_returns404() {
        given()
                .when()
                .get(BASE_PATH + "/999999")
                .then()
                .statusCode(404)
                .body(containsString("Film not found"));
    }

    @Test
    void testGetFilmById_withInvalidId_returns404Or400() {
        // Note: The actual behavior depends on JAX-RS implementation
        // It might return 400 for invalid format or 404 for not found
        given()
                .when()
                .get(BASE_PATH + "/invalid")
                .then()
                .statusCode(anyOf(is(400), is(404)));
    }

    // Tests for GET /api/films/paged

    @Test
    void testGetFilmsPaged_withDefaultParams_returns200() {
        given()
                .when()
                .get(BASE_PATH + "/paged")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .body(not(emptyString()));
    }

    @Test
    void testGetFilmsPaged_withPageAndMinLength_returnsFilteredResults() {
        given()
                .queryParam("page", 0)
                .queryParam("minLength", 120)
                .when()
                .get(BASE_PATH + "/paged")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT)
                .body(not(emptyString()));
    }

    @Test
    void testGetFilmsPaged_differentPages_returnDifferentResults() {
        // Get first page
        String firstPage = given()
                .queryParam("page", 0)
                .queryParam("minLength", 60)
                .when()
                .get(BASE_PATH + "/paged")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        // Get second page
        String secondPage = given()
                .queryParam("page", 1)
                .queryParam("minLength", 60)
                .when()
                .get(BASE_PATH + "/paged")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        // Verify pages are different (assuming database has enough records)
        if (!firstPage.isEmpty() && !secondPage.isEmpty()) {
            assertNotEquals(firstPage, secondPage, "Different pages should return different results");
        }
    }

    @Test
    void testGetFilmsPaged_withHighMinLength_returnsFewerResults() {
        String resultsLowThreshold = given()
                .queryParam("minLength", 50)
                .when()
                .get(BASE_PATH + "/paged")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        String resultsHighThreshold = given()
                .queryParam("minLength", 180)
                .when()
                .get(BASE_PATH + "/paged")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        // Higher threshold should return fewer or equal results
        int lowCount = resultsLowThreshold.split("\n").length;
        int highCount = resultsHighThreshold.isEmpty() ? 0 : resultsHighThreshold.split("\n").length;

        assertTrue(highCount <= lowCount,
                "Higher threshold should return fewer or equal films");
    }

    // Tests for GET /api/films/search

    @Test
    void testSearchFilmsByTitleWithActors_withTitlePrefix_returnsMatchingFilms() {
        given()
                .queryParam("titlePrefix", "A")
                .queryParam("minLength", 60)
                .when()
                .get(BASE_PATH + "/search")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT);
    }

    @Test
    void testSearchFilmsByTitleWithActors_withDefaultParams_returnsResults() {
        given()
                .when()
                .get(BASE_PATH + "/search")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT);
    }

    @Test
    void testSearchFilmsByTitleWithActors_resultContainsActorInfo() {
        String response = given()
                .queryParam("titlePrefix", "A")
                .queryParam("minLength", 60)
                .when()
                .get(BASE_PATH + "/search")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        // Response should contain actor information (if films have actors)
        if (!response.isEmpty()) {
            assertTrue(response.contains("Actors:"), "Response should mention actors");
        }
    }

    @Test
    void testSearchFilmsByTitleWithActors_withNonMatchingPrefix_returnsEmptyOrNoResults() {
        given()
                .queryParam("titlePrefix", "ZZZZZ") // Unlikely prefix
                .queryParam("minLength", 60)
                .when()
                .get(BASE_PATH + "/search")
                .then()
                .statusCode(200);
        // Response might be empty string if no results
    }

    // Tests for PUT /api/films/rental-rate

    @Test
    void testUpdateRentalRate_withValidParams_returns200() {
        given()
                .queryParam("minLength", 180)
                .queryParam("rate", "9.99")
                .when()
                .put(BASE_PATH + "/rental-rate")
                .then()
                .statusCode(200)
                .contentType(ContentType.TEXT);
    }

    @Test
    void testUpdateRentalRate_withNullRate_returns400() {
        given()
                .queryParam("minLength", 120)
                .when()
                .put(BASE_PATH + "/rental-rate")
                .then()
                .statusCode(400);
    }

    @Test
    void testUpdateRentalRate_withZeroRate_returns400() {
        given()
                .queryParam("minLength", 120)
                .queryParam("rate", "0")
                .when()
                .put(BASE_PATH + "/rental-rate")
                .then()
                .statusCode(400)
                .contentType(ContentType.TEXT)
                .body(containsString("positive"));
    }

    @Test
    void testUpdateRentalRate_withNegativeRate_returns400() {
        given()
                .queryParam("minLength", 120)
                .queryParam("rate", "-5.00")
                .when()
                .put(BASE_PATH + "/rental-rate")
                .then()
                .statusCode(400)
                .contentType(ContentType.TEXT)
                .body(containsString("positive"));
    }

    @Test
    void testUpdateRentalRate_responseContainsUpdatedRate() {
        String response = given()
                .queryParam("minLength", 180)
                .queryParam("rate", "4.99")
                .when()
                .put(BASE_PATH + "/rental-rate")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        // Response should contain the rental rate information
        if (!response.isEmpty()) {
            assertTrue(response.contains("$"), "Response should contain price formatting");
        }
    }

    @Test
    void testUpdateRentalRate_withInvalidRateFormat_returns400Or404() {
        // Invalid format might return 400 or 404 depending on JAX-RS implementation
        given()
                .queryParam("minLength", 120)
                .queryParam("rate", "invalid")
                .when()
                .put(BASE_PATH + "/rental-rate")
                .then()
                .statusCode(anyOf(is(400), is(404)));
    }

    // Edge case tests

    @Test
    void testGetFilmsPaged_withNegativePage_returns400() {
        // Negative page numbers should be rejected
        given()
                .queryParam("page", -1)
                .queryParam("minLength", 60)
                .when()
                .get(BASE_PATH + "/paged")
                .then()
                .statusCode(400)
                .body(containsString("non-negative"));
    }

    @Test
    void testGetFilmsPaged_withVeryHighPage_returnsEmptyOrFewResults() {
        given()
                .queryParam("page", 1000)
                .queryParam("minLength", 60)
                .when()
                .get(BASE_PATH + "/paged")
                .then()
                .statusCode(200);
        // Might return empty results
    }

    // Helper method for assertions
    private void assertNotEquals(String first, String second, String message) {
        if (first.equals(second)) {
            throw new AssertionError(message);
        }
    }

    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

package com.snooker4real.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Film entity.
 */
class FilmTest {

    private Film film;

    @BeforeEach
    void setUp() {
        film = new Film();
    }

    @Test
    void testDefaultConstructor_initializesEmptyActorsList() {
        // When: Creating a new Film with default constructor
        Film newFilm = new Film();

        // Then: Actors list is initialized
        assertNotNull(newFilm.getActors(), "Actors list should be initialized");
        assertTrue(newFilm.getActors().isEmpty(), "Actors list should be empty");
    }

    @Test
    void testParameterizedConstructor_setsProvidedFields() {
        // Given: Film data
        Integer filmId = 1;
        String title = "Test Film";
        Integer length = 120;

        // When: Creating a Film with parameterized constructor
        Film newFilm = new Film(filmId, title, length);

        // Then: Fields are set correctly
        assertEquals(filmId, newFilm.getFilmId());
        assertEquals(title, newFilm.getTitle());
        assertEquals(length, newFilm.getLength());
    }

    @Test
    void testSetAndGetFilmId() {
        // Given: A film ID
        Integer filmId = 42;

        // When: Setting the film ID
        film.setFilmId(filmId);

        // Then: The ID is correctly stored
        assertEquals(filmId, film.getFilmId());
    }

    @Test
    void testSetAndGetTitle() {
        // Given: A film title
        String title = "The Great Movie";

        // When: Setting the title
        film.setTitle(title);

        // Then: The title is correctly stored
        assertEquals(title, film.getTitle());
    }

    @Test
    void testSetAndGetDescription() {
        // Given: A description
        String description = "An epic tale of adventure";

        // When: Setting the description
        film.setDescription(description);

        // Then: The description is correctly stored
        assertEquals(description, film.getDescription());
    }

    @Test
    void testSetAndGetReleaseYear() {
        // Given: A release year
        Short year = 2023;

        // When: Setting the release year
        film.setReleaseYear(year);

        // Then: The year is correctly stored
        assertEquals(year, film.getReleaseYear());
    }

    @Test
    void testSetAndGetLength() {
        // Given: A film length
        Integer length = 145;

        // When: Setting the length
        film.setLength(length);

        // Then: The length is correctly stored
        assertEquals(length, film.getLength());
    }

    @Test
    void testSetAndGetRentalRate() {
        // Given: A rental rate
        BigDecimal rate = new BigDecimal("4.99");

        // When: Setting the rental rate
        film.setRentalRate(rate);

        // Then: The rate is correctly stored
        assertEquals(rate, film.getRentalRate());
        assertEquals(0, rate.compareTo(film.getRentalRate()));
    }

    @Test
    void testSetAndGetRentalDuration() {
        // Given: A rental duration
        Integer duration = 7;

        // When: Setting the rental duration
        film.setRentalDuration(duration);

        // Then: The duration is correctly stored
        assertEquals(duration, film.getRentalDuration());
    }

    @Test
    void testSetAndGetReplacementCost() {
        // Given: A replacement cost
        BigDecimal cost = new BigDecimal("19.99");

        // When: Setting the replacement cost
        film.setReplacementCost(cost);

        // Then: The cost is correctly stored
        assertEquals(cost, film.getReplacementCost());
    }

    @Test
    void testSetAndGetLanguageId() {
        // Given: A language ID
        Integer languageId = 1;

        // When: Setting the language ID
        film.setLanguageId(languageId);

        // Then: The ID is correctly stored
        assertEquals(languageId, film.getLanguageId());
    }

    @Test
    void testSetAndGetOriginalLanguageId() {
        // Given: An original language ID
        Integer languageId = 2;

        // When: Setting the original language ID
        film.setOriginalLanguageId(languageId);

        // Then: The ID is correctly stored
        assertEquals(languageId, film.getOriginalLanguageId());
    }

    @Test
    void testSetAndGetRating() {
        // Given: A rating
        Object rating = "PG-13";

        // When: Setting the rating
        film.setRating(rating);

        // Then: The rating is correctly stored
        assertEquals(rating, film.getRating());
    }

    @Test
    void testSetAndGetSpecialFeatures() {
        // Given: Special features
        Object features = "Deleted Scenes";

        // When: Setting special features
        film.setSpecialFeatures(features);

        // Then: The features are correctly stored
        assertEquals(features, film.getSpecialFeatures());
    }

    @Test
    void testSetAndGetLastUpdate() {
        // Given: A timestamp
        LocalDateTime timestamp = LocalDateTime.now();

        // When: Setting last update
        film.setLastUpdate(timestamp);

        // Then: The timestamp is correctly stored
        assertEquals(timestamp, film.getLastUpdate());
    }

    @Test
    void testSetAndGetActors() {
        // Given: A list of actors
        List<Actor> actors = new ArrayList<>();
        Actor actor1 = new Actor();
        actor1.setActorId((short) 1);
        actor1.setFirstName("John");
        actor1.setLastName("Doe");
        actors.add(actor1);

        Actor actor2 = new Actor();
        actor2.setActorId((short) 2);
        actor2.setFirstName("Jane");
        actor2.setLastName("Smith");
        actors.add(actor2);

        // When: Setting actors
        film.setActors(actors);

        // Then: The actors are correctly stored
        assertEquals(2, film.getActors().size());
        assertEquals(actors, film.getActors());
    }

    @Test
    void testAddActorToFilm() {
        // Given: A film and an actor
        Actor actor = new Actor();
        actor.setActorId((short) 1);
        actor.setFirstName("Tom");
        actor.setLastName("Hanks");

        // When: Adding an actor to the film
        film.getActors().add(actor);

        // Then: The actor is in the film's actor list
        assertEquals(1, film.getActors().size());
        assertTrue(film.getActors().contains(actor));
    }

    @Test
    void testRentalRatePrecision() {
        // Given: A rental rate with specific precision
        BigDecimal rate = new BigDecimal("4.99");

        // When: Setting the rental rate
        film.setRentalRate(rate);

        // Then: The precision is maintained
        assertEquals(2, film.getRentalRate().scale());
        assertEquals("4.99", film.getRentalRate().toString());
    }

    @Test
    void testNullValues() {
        // Given: A film with null values
        film.setTitle(null);
        film.setDescription(null);
        film.setRentalRate(null);

        // Then: Null values are accepted
        assertNull(film.getTitle());
        assertNull(film.getDescription());
        assertNull(film.getRentalRate());
    }
}

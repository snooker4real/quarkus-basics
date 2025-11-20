package com.snooker4real.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Actor entity.
 */
class ActorTest {

    private Actor actor;

    @BeforeEach
    void setUp() {
        actor = new Actor();
    }

    @Test
    void testDefaultConstructor_initializesEmptyFilmsSet() {
        // When: Creating a new Actor with default constructor
        Actor newActor = new Actor();

        // Then: Films set is initialized
        assertNotNull(newActor.getFilms(), "Films set should be initialized");
        assertTrue(newActor.getFilms().isEmpty(), "Films set should be empty");
    }

    @Test
    void testSetAndGetActorId() {
        // Given: An actor ID
        Short actorId = 42;

        // When: Setting the actor ID
        actor.setActorId(actorId);

        // Then: The ID is correctly stored
        assertEquals(actorId, actor.getActorId());
    }

    @Test
    void testSetAndGetFirstName() {
        // Given: A first name
        String firstName = "John";

        // When: Setting the first name
        actor.setFirstName(firstName);

        // Then: The first name is correctly stored
        assertEquals(firstName, actor.getFirstName());
    }

    @Test
    void testSetAndGetLastName() {
        // Given: A last name
        String lastName = "Doe";

        // When: Setting the last name
        actor.setLastName(lastName);

        // Then: The last name is correctly stored
        assertEquals(lastName, actor.getLastName());
    }

    @Test
    void testSetAndGetLastUpdate() {
        // Given: A timestamp
        LocalDateTime timestamp = LocalDateTime.now();

        // When: Setting last update
        actor.setLastUpdate(timestamp);

        // Then: The timestamp is correctly stored
        assertEquals(timestamp, actor.getLastUpdate());
    }

    @Test
    void testSetAndGetFilms() {
        // Given: A set of films
        Set<Film> films = new HashSet<>();
        Film film1 = new Film();
        film1.setFilmId(1);
        film1.setTitle("Movie 1");
        films.add(film1);

        Film film2 = new Film();
        film2.setFilmId(2);
        film2.setTitle("Movie 2");
        films.add(film2);

        // When: Setting films
        actor.setFilms(films);

        // Then: The films are correctly stored
        assertEquals(2, actor.getFilms().size());
        assertEquals(films, actor.getFilms());
    }

    @Test
    void testAddFilmToActor() {
        // Given: An actor and a film
        Film film = new Film();
        film.setFilmId(1);
        film.setTitle("Test Movie");

        // When: Adding a film to the actor
        actor.getFilms().add(film);

        // Then: The film is in the actor's film set
        assertEquals(1, actor.getFilms().size());
        assertTrue(actor.getFilms().contains(film));
    }

    @Test
    void testFilmsSetDoesNotAllowDuplicates() {
        // Given: An actor and a film
        Film film = new Film();
        film.setFilmId(1);
        film.setTitle("Test Movie");

        // When: Adding the same film twice
        actor.getFilms().add(film);
        actor.getFilms().add(film);

        // Then: The film appears only once (Set behavior)
        assertEquals(1, actor.getFilms().size());
    }

    @Test
    void testActorFullName() {
        // Given: An actor with first and last name
        actor.setFirstName("Tom");
        actor.setLastName("Hanks");

        // When: Getting full name (manual concatenation)
        String fullName = actor.getFirstName() + " " + actor.getLastName();

        // Then: Full name is correct
        assertEquals("Tom Hanks", fullName);
    }

    @Test
    void testNullValues() {
        // Given: An actor with null values
        actor.setFirstName(null);
        actor.setLastName(null);
        actor.setLastUpdate(null);

        // Then: Null values are accepted
        assertNull(actor.getFirstName());
        assertNull(actor.getLastName());
        assertNull(actor.getLastUpdate());
    }

    @Test
    void testActorIdAsShort() {
        // Given: Maximum short value
        Short maxShort = Short.MAX_VALUE;

        // When: Setting actor ID
        actor.setActorId(maxShort);

        // Then: Short type is correctly handled
        assertEquals(maxShort, actor.getActorId());
        assertEquals(Short.class, actor.getActorId().getClass());
    }

    @Test
    void testBidirectionalRelationship() {
        // Given: An actor and a film
        Film film = new Film();
        film.setFilmId(1);
        film.setTitle("Test Movie");

        // When: Setting up bidirectional relationship
        actor.getFilms().add(film);
        film.getActors().add(actor);

        // Then: Both sides of the relationship are established
        assertTrue(actor.getFilms().contains(film));
        assertTrue(film.getActors().contains(actor));
    }
}

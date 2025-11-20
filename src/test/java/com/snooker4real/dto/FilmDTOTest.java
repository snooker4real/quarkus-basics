package com.snooker4real.dto;

import com.snooker4real.model.Actor;
import com.snooker4real.model.Film;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FilmDTO demonstrating Java 25 record features.
 */
class FilmDTOTest {

    @Test
    void testRecordCreation() {
        // Given: Film data
        var dto = new FilmDTO(
                1,
                "Test Film",
                "Description",
                (short) 2023,
                120,
                new BigDecimal("4.99"),
                new BigDecimal("19.99"),
                "PG-13",
                List.of()
        );

        // Then: All fields are accessible via record accessors
        assertEquals(1, dto.filmId());
        assertEquals("Test Film", dto.title());
        assertEquals(120, dto.length());
    }

    @Test
    void testCompactConstructorValidation() {
        // When: Creating DTO with blank title
        // Then: Exception is thrown
        assertThrows(IllegalArgumentException.class, () ->
                new FilmDTO(1, "", "Desc", (short) 2023, 120,
                        BigDecimal.ONE, BigDecimal.TEN, "PG", null)
        );
    }

    @Test
    void testFromEntity() {
        // Given: A Film entity
        Film film = new Film();
        film.setFilmId(1);
        film.setTitle("Test Movie");
        film.setLength(100);
        film.setRentalRate(new BigDecimal("3.99"));

        // When: Converting to DTO
        FilmDTO dto = FilmDTO.fromEntity(film);

        // Then: DTO is created correctly
        assertEquals(film.getFilmId(), dto.filmId());
        assertEquals(film.getTitle(), dto.title());
        assertNotNull(dto.actors());
    }

    @Test
    void testGetCategory_usingPatternMatching() {
        // Test all categories using pattern matching
        assertEquals(FilmDTO.FilmCategory.SHORT,
                createFilmDTO(30).getCategory());
        assertEquals(FilmDTO.FilmCategory.FEATURE,
                createFilmDTO(90).getCategory());
        assertEquals(FilmDTO.FilmCategory.LONG,
                createFilmDTO(150).getCategory());
        assertEquals(FilmDTO.FilmCategory.EPIC,
                createFilmDTO(200).getCategory());
    }

    @Test
    void testGetFormattedDisplay_withNoActors() {
        // Given: Film with no actors
        var dto = createFilmDTO(120);

        // When: Getting formatted display
        String display = dto.getFormattedDisplay();

        // Then: Display shows rental rate
        assertTrue(display.contains("$4.99"));
        assertTrue(display.contains("120 min"));
    }

    @Test
    void testGetFormattedDisplay_withOneActor() {
        // Given: Film with one actor
        var actor = new ActorDTO((short) 1, "Tom", "Hanks");
        var dto = new FilmDTO(1, "Test", "Desc", (short) 2023, 120,
                new BigDecimal("4.99"), BigDecimal.TEN, "PG", List.of(actor));

        // When: Getting formatted display
        String display = dto.getFormattedDisplay();

        // Then: Display shows starring actor
        assertTrue(display.contains("starring"));
        assertTrue(display.contains("Tom Hanks"));
    }

    @Test
    void testGetFormattedDisplay_withMultipleActors() {
        // Given: Film with multiple actors
        var actors = List.of(
                new ActorDTO((short) 1, "Tom", "Hanks"),
                new ActorDTO((short) 2, "Matt", "Damon")
        );
        var dto = new FilmDTO(1, "Test", "Desc", (short) 2023, 120,
                new BigDecimal("4.99"), BigDecimal.TEN, "PG", actors);

        // When: Getting formatted display
        String display = dto.getFormattedDisplay();

        // Then: Display shows actor count
        assertTrue(display.contains("with 2 actors"));
    }

    @Test
    void testCalculateRentalCost() {
        // Given: Film with rental rate
        var dto = createFilmDTO(120);

        // When: Calculating cost for 3 days
        BigDecimal cost = dto.calculateRentalCost(3);

        // Then: Cost is calculated correctly
        assertEquals(new BigDecimal("14.97"), cost);
    }

    @Test
    void testIsFeatureLength() {
        assertTrue(createFilmDTO(40).isFeatureLength());
        assertTrue(createFilmDTO(120).isFeatureLength());
        assertFalse(createFilmDTO(39).isFeatureLength());
    }

    @Test
    void testRecordEquality() {
        // Records automatically implement equals()
        var dto1 = createFilmDTO(120);
        var dto2 = createFilmDTO(120);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testRecordImmutability() {
        // Records are immutable - can't modify after creation
        var dto = createFilmDTO(120);
        var originalTitle = dto.title();

        // The only way to "change" is to create a new record
        var newDto = new FilmDTO(
                dto.filmId(),
                "New Title",  // Changed
                dto.description(),
                dto.releaseYear(),
                dto.length(),
                dto.rentalRate(),
                dto.replacementCost(),
                dto.rating(),
                dto.actors()
        );

        assertEquals(originalTitle, dto.title());  // Original unchanged
        assertEquals("New Title", newDto.title()); // New record has new value
    }

    @Test
    void testSequencedCollectionFeatures() {
        // Given: Film with actors (sequenced collection)
        var actors = List.of(
                new ActorDTO((short) 1, "First", "Actor"),
                new ActorDTO((short) 2, "Second", "Actor"),
                new ActorDTO((short) 3, "Third", "Actor")
        );
        var dto = new FilmDTO(1, "Test", "Desc", (short) 2023, 120,
                new BigDecimal("4.99"), BigDecimal.TEN, "PG", actors);

        // Then: Can use sequenced collection methods
        assertEquals("First Actor", dto.actors().getFirst().fullName());
        assertEquals("Third Actor", dto.actors().getLast().fullName());
    }

    // Helper method
    private FilmDTO createFilmDTO(int length) {
        return new FilmDTO(
                1,
                "Test Film",
                "Description",
                (short) 2023,
                length,
                new BigDecimal("4.99"),
                new BigDecimal("19.99"),
                "PG-13",
                List.of()
        );
    }
}

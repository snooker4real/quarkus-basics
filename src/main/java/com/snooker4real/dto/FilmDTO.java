package com.snooker4real.dto;

import com.snooker4real.model.Film;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object for Film using Java 25 record feature.
 * Records provide immutable data carriers with automatic implementations of:
 * - Constructor
 * - Getters
 * - equals(), hashCode(), toString()
 */
public record FilmDTO(
        Integer filmId,
        String title,
        String description,
        Short releaseYear,
        Integer length,
        BigDecimal rentalRate,
        BigDecimal replacementCost,
        String rating,
        List<ActorDTO> actors
) {
    /**
     * Compact constructor for validation.
     * This is a Java 25 feature that allows validation without explicit parameter assignment.
     */
    public FilmDTO {
        // Validate required fields
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }

        // Ensure actors list is not null (use empty list instead)
        if (actors == null) {
            actors = List.of();
        }
    }

    /**
     * Factory method to create FilmDTO from Film entity.
     * Uses modern Java features for clean, expressive code.
     */
    public static FilmDTO fromEntity(Film film) {
        return new FilmDTO(
                film.getFilmId(),
                film.getTitle(),
                film.getDescription(),
                film.getReleaseYear(),
                film.getLength(),
                film.getRentalRate(),
                film.getReplacementCost(),
                film.getRating() instanceof String s ? s : null,
                film.getActors() != null
                        ? film.getActors().stream()
                                .map(ActorDTO::fromEntity)
                                .toList()
                        : List.of()
        );
    }

    /**
     * Factory method for summary view (without actors).
     */
    public static FilmDTO summaryFromEntity(Film film) {
        return new FilmDTO(
                film.getFilmId(),
                film.getTitle(),
                film.getDescription(),
                film.getReleaseYear(),
                film.getLength(),
                film.getRentalRate(),
                film.getReplacementCost(),
                film.getRating() instanceof String s ? s : null,
                List.of()
        );
    }

    /**
     * Get formatted display string using pattern matching.
     * Demonstrates Java 25 enhanced switch expressions and pattern matching.
     */
    public String getFormattedDisplay() {
        return switch (this) {
            case FilmDTO dto when dto.actors().isEmpty() ->
                    "%s (%d min) - $%.2f".formatted(dto.title(), dto.length(), dto.rentalRate());
            case FilmDTO dto when dto.actors().size() == 1 ->
                    "%s (%d min) starring %s".formatted(
                            dto.title(), dto.length(), dto.actors().getFirst().fullName());
            case FilmDTO dto ->
                    "%s (%d min) with %d actors".formatted(
                            dto.title(), dto.length(), dto.actors().size());
        };
    }

    /**
     * Calculate total rental cost based on rental duration.
     */
    public BigDecimal calculateRentalCost(int days) {
        return rentalRate != null
                ? rentalRate.multiply(BigDecimal.valueOf(days))
                : BigDecimal.ZERO;
    }

    /**
     * Check if film is a feature-length film (>= 40 minutes).
     */
    public boolean isFeatureLength() {
        return length != null && length >= 40;
    }

    /**
     * Get film category based on length using pattern matching.
     */
    public FilmCategory getCategory() {
        return switch (length) {
            case null -> FilmCategory.UNKNOWN;
            case Integer l when l < 40 -> FilmCategory.SHORT;
            case Integer l when l < 120 -> FilmCategory.FEATURE;
            case Integer l when l < 180 -> FilmCategory.LONG;
            default -> FilmCategory.EPIC;
        };
    }

    /**
     * Film category enum using modern Java features.
     */
    public enum FilmCategory {
        UNKNOWN("Unknown Length"),
        SHORT("Short Film"),
        FEATURE("Feature Film"),
        LONG("Long Film"),
        EPIC("Epic Film");

        private final String displayName;

        FilmCategory(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }
}

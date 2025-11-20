package com.snooker4real.service;

import com.snooker4real.dto.FilmDTO;
import com.snooker4real.model.Film;
import com.snooker4real.repository.FilmRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Film business logic.
 * Demonstrates modern Java 25 features including:
 * - Pattern matching
 * - Switch expressions
 * - Sequenced collections
 * - Enhanced instanceof
 */
@ApplicationScoped
public class FilmService {

    @Inject
    FilmRepository filmRepository;

    /**
     * Find a film by ID and return as DTO.
     * Uses pattern matching and Optional enhancements.
     */
    @Transactional
    public Optional<FilmDTO> findFilmById(Integer filmId) {
        return filmRepository.findById(filmId)
                .map(film -> {
                    // Initialize lazy collections within transaction
                    film.getActors().size(); // Force initialization
                    return FilmDTO.fromEntity(film);
                });
    }

    /**
     * Get paginated films using sequenced collections.
     * Demonstrates modern collection methods.
     */
    public PaginatedResult<FilmDTO> getFilmsPaginated(long page, int minLength, int pageSize) {
        List<FilmDTO> films = filmRepository.findByMinimumLengthPaged(page, minLength)
                .map(FilmDTO::summaryFromEntity)
                .toList();

        return new PaginatedResult<>(
                films,
                page,
                pageSize,
                !films.isEmpty() && films.size() == pageSize
        );
    }

    /**
     * Search films by title prefix with categorization.
     * Uses pattern matching and enhanced switch expressions.
     */
    public SearchResult searchFilms(String titlePrefix, int minLength) {
        List<FilmDTO> films = filmRepository.findByTitlePrefixWithActors(titlePrefix, minLength)
                .map(FilmDTO::fromEntity)
                .toList();

        // Categorize films using modern Java features
        var categorized = films.stream()
                .collect(java.util.stream.Collectors.groupingBy(FilmDTO::getCategory));

        return new SearchResult(
                films,
                films.size(),
                categorized
        );
    }

    /**
     * Update rental rates with validation and result reporting.
     * Demonstrates pattern matching in method parameters.
     */
    public RentalRateUpdateResult updateRentalRates(int minLength, BigDecimal newRate) {
        // Validate using pattern matching
        var validationResult = validateRentalRate(newRate);
        if (validationResult instanceof ValidationResult.Invalid(String reason)) {
            return new RentalRateUpdateResult.Failed(reason);
        }

        try {
            filmRepository.updateRentalRate(minLength, newRate);

            List<FilmDTO> updatedFilms = filmRepository.findByMinimumLength(minLength)
                    .limit(100)
                    .map(FilmDTO::summaryFromEntity)
                    .toList();

            return new RentalRateUpdateResult.Success(
                    updatedFilms.size(),
                    newRate,
                    updatedFilms
            );
        } catch (Exception e) {
            return new RentalRateUpdateResult.Failed(
                    "Update failed: " + e.getMessage()
            );
        }
    }

    /**
     * Validate rental rate using sealed interfaces (Java 25 feature).
     */
    private ValidationResult validateRentalRate(BigDecimal rate) {
        return switch (rate) {
            case null -> new ValidationResult.Invalid("Rental rate cannot be null");
            case BigDecimal r when r.compareTo(BigDecimal.ZERO) <= 0 ->
                    new ValidationResult.Invalid("Rental rate must be positive");
            case BigDecimal r when r.compareTo(new BigDecimal("1000")) > 0 ->
                    new ValidationResult.Invalid("Rental rate too high");
            default -> new ValidationResult.Valid();
        };
    }

    /**
     * Get film statistics using modern collection operations.
     */
    public FilmStatistics getStatistics(int minLength) {
        List<Film> films = filmRepository.findByMinimumLength(minLength)
                .limit(1000)
                .toList();

        if (films.isEmpty()) {
            return new FilmStatistics(0, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0);
        }

        var rates = films.stream()
                .map(Film::getRentalRate)
                .filter(rate -> rate != null)
                .sorted()
                .toList();

        BigDecimal avgRate = rates.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(rates.size()), 2, java.math.RoundingMode.HALF_UP);

        return new FilmStatistics(
                films.size(),
                avgRate,
                rates.isEmpty() ? BigDecimal.ZERO : rates.getLast(), // Sequenced collection feature
                films.stream().mapToInt(Film::getLength).min().orElse(0),
                films.stream().mapToInt(Film::getLength).max().orElse(0)
        );
    }

    // Modern sealed interfaces for type-safe results (Java 25 feature)

    /**
     * Sealed interface for validation results.
     */
    sealed interface ValidationResult permits ValidationResult.Valid, ValidationResult.Invalid {
        record Valid() implements ValidationResult {}
        record Invalid(String reason) implements ValidationResult {}
    }

    /**
     * Sealed interface for rental rate update results.
     */
    public sealed interface RentalRateUpdateResult {
        record Success(
                int filmsUpdated,
                BigDecimal newRate,
                List<FilmDTO> films
        ) implements RentalRateUpdateResult {}

        record Failed(String reason) implements RentalRateUpdateResult {}
    }

    /**
     * Record for paginated results using sequenced collections.
     */
    public record PaginatedResult<T>(
            List<T> items,
            long currentPage,
            int pageSize,
            boolean hasMore
    ) {
        public T getFirst() {
            return items.isEmpty() ? null : items.getFirst();
        }

        public T getLast() {
            return items.isEmpty() ? null : items.getLast();
        }
    }

    /**
     * Record for search results with categorization.
     */
    public record SearchResult(
            List<FilmDTO> films,
            int totalCount,
            java.util.Map<FilmDTO.FilmCategory, List<FilmDTO>> byCategory
    ) {}

    /**
     * Record for film statistics.
     */
    public record FilmStatistics(
            int totalFilms,
            BigDecimal averageRentalRate,
            BigDecimal maxRentalRate,
            int minLength,
            int maxLength
    ) {}
}

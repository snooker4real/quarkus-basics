package com.snooker4real.repository;

import com.snooker4real.model.Film;
import com.snooker4real.model.Film$;
import com.speedment.jpastreamer.application.JPAStreamer;
import com.speedment.jpastreamer.projection.Projection;
import com.speedment.jpastreamer.streamconfiguration.StreamConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Repository for Film entity operations using JPAStreamer for efficient database queries.
 */
@ApplicationScoped
public class FilmRepository {

    private static final int PAGE_SIZE = 20;

    @Inject
    JPAStreamer jpaStreamer;

    /**
     * Finds a film by its ID.
     *
     * @param filmId the film ID
     * @return an Optional containing the film if found
     */
    public Optional<Film> findById(Integer filmId) {
        return jpaStreamer.stream(Film.class)
                .filter(film -> film.getFilmId().equals(filmId))
                .findFirst();
    }

    /**
     * Finds all films with a minimum length, sorted by length.
     *
     * @param minLengthMinutes minimum film length in minutes
     * @return stream of films matching the criteria
     */
    public Stream<Film> findByMinimumLength(int minLengthMinutes) {
        return jpaStreamer.stream(Film.class)
                .filter(Film$.length.greaterThan(minLengthMinutes))
                .sorted(Film$.length);
    }

    /**
     * Finds films with pagination and minimum length filter.
     * Uses projection to fetch only required fields for better performance.
     *
     * @param page page number (zero-based)
     * @param minLengthMinutes minimum film length in minutes
     * @return stream of films for the requested page
     */
    public Stream<Film> findByMinimumLengthPaged(long page, int minLengthMinutes) {
        return jpaStreamer.stream(Projection.select(Film$.filmId, Film$.title, Film$.length))
                .filter(Film$.length.greaterThan(minLengthMinutes))
                .sorted(Film$.length)
                .skip(PAGE_SIZE * page)
                .limit(PAGE_SIZE);
    }

    /**
     * Finds films by title prefix and minimum length, with actors eagerly loaded.
     * Results are sorted by length in descending order.
     *
     * @param titlePrefix prefix to match film titles
     * @param minLengthMinutes minimum film length in minutes
     * @return stream of films with actors loaded
     */
    public Stream<Film> findByTitlePrefixWithActors(String titlePrefix, int minLengthMinutes) {
        StreamConfiguration<Film> config = StreamConfiguration.of(Film.class)
                .joining(Film$.actors);

        return jpaStreamer.stream(config)
                .filter(Film$.title.startsWith(titlePrefix)
                        .and(Film$.length.greaterThan(minLengthMinutes)))
                .sorted(Film$.length.reversed());
    }

    /**
     * Updates the rental rate for all films with a minimum length.
     *
     * @param minLengthMinutes minimum film length in minutes
     * @param newRentalRate new rental rate to set
     */
    @Transactional
    public void updateRentalRate(int minLengthMinutes, BigDecimal newRentalRate) {
        jpaStreamer.stream(Film.class)
                .filter(Film$.length.greaterThan(minLengthMinutes))
                .forEach(film -> film.setRentalRate(newRentalRate));
    }
}

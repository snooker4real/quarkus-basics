package com.snooker4real.controller;

import com.snooker4real.model.Actor;
import com.snooker4real.model.Film;
import com.snooker4real.repository.FilmRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * REST resource for Film operations.
 */
@Path("/api/films")
@Produces(MediaType.TEXT_PLAIN)
public class FilmResource {

    @Inject
    FilmRepository filmRepository;

    /**
     * Get a film by its ID.
     *
     * @param filmId the film ID
     * @return the film title or 404 if not found
     */
    @GET
    @Path("/{filmId}")
    public Response getFilmById(@PathParam("filmId") Integer filmId) {
        return filmRepository.findById(filmId)
                .map(Film::getTitle)
                .map(title -> Response.ok(title).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Film not found with ID: " + filmId)
                        .build());
    }

    /**
     * Get paginated list of films with minimum length filter.
     *
     * @param page page number (zero-based, default 0)
     * @param minLength minimum film length in minutes
     * @return formatted list of films
     */
    @GET
    @Path("/paged")
    public String getFilmsPaged(
            @QueryParam("page") @DefaultValue("0") long page,
            @QueryParam("minLength") @DefaultValue("0") int minLength) {

        return filmRepository.findByMinimumLengthPaged(page, minLength)
                .map(this::formatFilmWithLength)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Get films by title prefix with actors.
     *
     * @param titlePrefix the title prefix to search for
     * @param minLength minimum film length in minutes
     * @return formatted list of films with actors
     */
    @GET
    @Path("/search")
    public String searchFilmsByTitleWithActors(
            @QueryParam("titlePrefix") @DefaultValue("") String titlePrefix,
            @QueryParam("minLength") @DefaultValue("0") int minLength) {

        return filmRepository.findByTitlePrefixWithActors(titlePrefix, minLength)
                .map(this::formatFilmWithActors)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Update rental rate for films with minimum length.
     *
     * @param minLength minimum film length in minutes
     * @param rentalRate new rental rate
     * @return updated list of films
     */
    @PUT
    @Path("/rental-rate")
    public String updateRentalRate(
            @QueryParam("minLength") @DefaultValue("0") int minLength,
            @QueryParam("rate") BigDecimal rentalRate) {

        if (rentalRate == null || rentalRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Rental rate must be a positive value");
        }

        filmRepository.updateRentalRate(minLength, rentalRate);

        return filmRepository.findByMinimumLength(minLength)
                .map(this::formatFilmWithRentalRate)
                .collect(Collectors.joining("\n"));
    }

    // Private formatting methods

    private String formatFilmWithLength(Film film) {
        return String.format("%s (%d min)", film.getTitle(), film.getLength());
    }

    private String formatFilmWithActors(Film film) {
        String actorNames = film.getActors().stream()
                .map(this::formatActorName)
                .collect(Collectors.joining(", "));

        return String.format("%s (%d min) - Actors: %s",
                film.getTitle(),
                film.getLength(),
                actorNames);
    }

    private String formatActorName(Actor actor) {
        return String.format("%s %s", actor.getFirstName(), actor.getLastName());
    }

    private String formatFilmWithRentalRate(Film film) {
        return String.format("%s (%d min) - $%.2f",
                film.getTitle(),
                film.getLength(),
                film.getRentalRate());
    }
}

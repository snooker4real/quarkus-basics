package com.snooker4real.controller;

import com.snooker4real.dto.FilmDTO;
import com.snooker4real.service.FilmService;
import com.snooker4real.service.FilmService.RentalRateUpdateResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;

/**
 * Modern REST resource using Java 25 features.
 * Demonstrates:
 * - Pattern matching in switch expressions
 * - Records for DTOs
 * - Sealed interfaces for result types
 * - Enhanced instanceof with pattern variables
 */
@Path("/api/v2/films")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FilmResourceV2 {

    @Inject
    FilmService filmService;

    /**
     * Get a film by ID.
     * Uses modern Optional handling and pattern matching.
     */
    @GET
    @Path("/{filmId}")
    public Response getFilmById(@PathParam("filmId") Integer filmId) {
        return filmService.findFilmById(filmId)
                .map(film -> Response.ok(film).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Film not found", filmId))
                        .build());
    }

    /**
     * Get paginated films.
     * Returns structured pagination info using records.
     */
    @GET
    public Response getFilmsPaginated(
            @QueryParam("page") @DefaultValue("0") long page,
            @QueryParam("minLength") @DefaultValue("0") int minLength,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize) {

        if (page < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Page must be non-negative", page))
                    .build();
        }

        var result = filmService.getFilmsPaginated(page, minLength, pageSize);
        return Response.ok(result).build();
    }

    /**
     * Search films by title prefix.
     * Returns categorized results.
     */
    @GET
    @Path("/search")
    public Response searchFilms(
            @QueryParam("titlePrefix") @DefaultValue("") String titlePrefix,
            @QueryParam("minLength") @DefaultValue("0") int minLength) {

        var result = filmService.searchFilms(titlePrefix, minLength);
        return Response.ok(result).build();
    }

    /**
     * Update rental rates.
     * Demonstrates pattern matching with sealed interfaces.
     */
    @PUT
    @Path("/rental-rate")
    public Response updateRentalRate(
            @QueryParam("minLength") @DefaultValue("0") int minLength,
            @QueryParam("rate") BigDecimal rate) {

        var result = filmService.updateRentalRates(minLength, rate);

        // Use pattern matching with sealed interfaces for type-safe result handling
        return switch (result) {
            case RentalRateUpdateResult.Success(var count, var newRate, var films) ->
                    Response.ok(new UpdateSuccessResponse(
                            "Successfully updated %d films".formatted(count),
                            count,
                            newRate,
                            films
                    )).build();

            case RentalRateUpdateResult.Failed(var reason) ->
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorResponse(reason, rate))
                            .build();
        };
    }

    /**
     * Get film statistics.
     */
    @GET
    @Path("/statistics")
    public Response getStatistics(
            @QueryParam("minLength") @DefaultValue("0") int minLength) {

        var stats = filmService.getStatistics(minLength);
        return Response.ok(stats).build();
    }

    /**
     * Get film by ID with specific format.
     * Demonstrates pattern matching in method logic.
     */
    @GET
    @Path("/{filmId}/formatted")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getFilmFormatted(
            @PathParam("filmId") Integer filmId,
            @QueryParam("format") @DefaultValue("standard") String format) {

        return filmService.findFilmById(filmId)
                .map(film -> formatFilm(film, format))
                .map(formatted -> Response.ok(formatted).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("Film not found")
                        .build());
    }

    /**
     * Format film using pattern matching switch.
     */
    private String formatFilm(FilmDTO film, String format) {
        return switch (format.toLowerCase()) {
            case "short" -> "%s (%d min)".formatted(film.title(), film.length());
            case "detailed" -> """
                    Title: %s
                    Length: %d minutes
                    Rental Rate: $%.2f
                    Category: %s
                    Actors: %d
                    """.formatted(
                    film.title(),
                    film.length(),
                    film.rentalRate(),
                    film.getCategory().displayName(),
                    film.actors().size());
            case "json" -> film.toString();
            default -> film.getFormattedDisplay();
        };
    }

    // Response records using Java 25 record feature

    /**
     * Error response record.
     */
    public record ErrorResponse(
            String message,
            Object details
    ) {}

    /**
     * Update success response record.
     */
    public record UpdateSuccessResponse(
            String message,
            int filmsUpdated,
            BigDecimal newRate,
            java.util.List<FilmDTO> films
    ) {}
}

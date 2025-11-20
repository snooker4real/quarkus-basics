package com.snooker4real.dto;

import com.snooker4real.model.Actor;

/**
 * Data Transfer Object for Actor using Java 25 record feature.
 */
public record ActorDTO(
        Short actorId,
        String firstName,
        String lastName
) {
    /**
     * Compact constructor for validation.
     */
    public ActorDTO {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be blank");
        }
    }

    /**
     * Factory method to create ActorDTO from Actor entity.
     */
    public static ActorDTO fromEntity(Actor actor) {
        return new ActorDTO(
                actor.getActorId(),
                actor.getFirstName(),
                actor.getLastName()
        );
    }

    /**
     * Get full name of the actor.
     */
    public String fullName() {
        return "%s %s".formatted(firstName, lastName);
    }

    /**
     * Get initials.
     */
    public String initials() {
        return "%c.%c.".formatted(
                firstName.charAt(0),
                lastName.charAt(0)
        );
    }
}

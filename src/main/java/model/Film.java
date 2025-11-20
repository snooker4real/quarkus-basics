package model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "film", schema = "sakila")
public class Film {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "film_id")
    private Integer filmId;

    @Basic
    @Column(name = "title")
    private String title;

    @Basic
    @Column(name = "description")
    private String description;

    @Basic
    @Column(name = "release_year")
    private Object releaseYear;

    @Basic
    @Column(name = "language_id")
    private Integer languageId;

    @Basic
    @Column(name = "original_language_id")
    private Integer originalLanguageId;

    @Basic
    @Column(name = "rental_duration")
    private Integer rentalDuration;

    @Basic
    @Column(name = "rental_rate")
    private BigDecimal rentalRate;

    @Basic
    @Column(name = "length")
    private Integer length;

    @Basic
    @Column(name = "replacement_cost")
    private BigDecimal replacementCost;

    @Basic
    @Column(name = "rating", columnDefinition = "enum('G', 'PG', 'PG-13', 'R', 'NC-17')")
    private Object rating;

    @Basic
    @Column(name = "special_features", columnDefinition = "enum('Trailers', 'Deleted Scenes', 'Commentaries', 'Behind the Scenes')")
    private Object specialFeatures;

    @Basic
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
            name = "film_actor",
            joinColumns = { @JoinColumn(name = "film_id") },
            inverseJoinColumns = { @JoinColumn(name = "actor_id") }
    )
    private List<Actor> actors = new ArrayList<>();

    public Integer getFilmId() {
        return this.filmId;
    }

    public void setFilmId(Integer filmId) {
        this.filmId = filmId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getReleaseYear() {
        return this.releaseYear;
    }

    public void setReleaseYear(Object releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Integer getLanguageId() {
        return this.languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public Integer getOriginalLanguageId() {
        return this.originalLanguageId;
    }

    public void setOriginalLanguageId(Integer originalLanguageId) {
        this.originalLanguageId = originalLanguageId;
    }

    public Integer getRentalDuration() {
        return this.rentalDuration;
    }

    public void setRentalDuration(Integer rentalDuration) {
        this.rentalDuration = rentalDuration;
    }

    public BigDecimal getRentalRate() {
        return this.rentalRate;
    }

    public void setRentalRate(BigDecimal rentalRate) {
        this.rentalRate = rentalRate;
    }

    public Integer getLength() {
        return this.length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public BigDecimal getReplacementCost() {
        return this.replacementCost;
    }

    public void setReplacementCost(BigDecimal replacementCost) {
        this.replacementCost = replacementCost;
    }

    public Object getRating() {
        return this.rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Object getSpecialFeatures() {
        return this.specialFeatures;
    }

    public void setSpecialFeatures(Object specialFeatures) {
        this.specialFeatures = specialFeatures;
    }

    public LocalDateTime getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setRating(Object rating) {
        this.rating = rating;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }
}

# Java 25 Features Implementation Guide

This document showcases the modern Java 25 features implemented in the quarkus-basics project.

## Table of Contents
1. [Records](#records)
2. [Pattern Matching](#pattern-matching)
3. [Switch Expressions](#switch-expressions)
4. [Sealed Interfaces](#sealed-interfaces)
5. [Sequenced Collections](#sequenced-collections)
6. [Text Blocks](#text-blocks)
7. [Enhanced instanceof](#enhanced-instanceof)

---

## Records

### What are Records?
Records are immutable data carriers that automatically provide constructors, getters, `equals()`, `hashCode()`, and `toString()`.

### Implementation Examples

#### Basic Record (FilmDTO.java)
```java
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
    // Compact constructor for validation
    public FilmDTO {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        if (actors == null) {
            actors = List.of();
        }
    }
}
```

#### Benefits:
- **Concise**: No boilerplate getters/setters
- **Immutable**: Thread-safe by default
- **Type-safe**: Compile-time field checking
- **Readable**: Clear data structure

---

## Pattern Matching

### What is Pattern Matching?
Pattern matching allows you to test and extract values from objects in a single operation.

### Implementation Examples

#### Pattern Matching in Switch (FilmDTO.java)
```java
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
```

#### Pattern Matching with Guards (FilmDTO.java)
```java
public FilmCategory getCategory() {
    return switch (length) {
        case null -> FilmCategory.UNKNOWN;
        case Integer l when l < 40 -> FilmCategory.SHORT;
        case Integer l when l < 120 -> FilmCategory.FEATURE;
        case Integer l when l < 180 -> FilmCategory.LONG;
        default -> FilmCategory.EPIC;
    };
}
```

#### Benefits:
- **Safer**: No ClassCastException risks
- **Concise**: Less code compared to traditional if-else chains
- **Expressive**: Intent is clearer

---

## Switch Expressions

### What are Switch Expressions?
Modern switch that returns values and doesn't require break statements.

### Implementation Examples

#### Switch with Sealed Types (FilmResourceV2.java)
```java
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
```

#### Benefits:
- **Expression-based**: Returns a value
- **Exhaustive**: Compiler ensures all cases covered
- **No fall-through**: Safer than traditional switch

---

## Sealed Interfaces

### What are Sealed Interfaces?
Sealed types restrict which classes can implement/extend them, enabling exhaustive pattern matching.

### Implementation Examples

#### Sealed Result Type (FilmService.java)
```java
public sealed interface RentalRateUpdateResult {
    record Success(
            int filmsUpdated,
            BigDecimal newRate,
            List<FilmDTO> films
    ) implements RentalRateUpdateResult {}

    record Failed(String reason) implements RentalRateUpdateResult {}
}
```

#### Validation Result (FilmService.java)
```java
sealed interface ValidationResult permits ValidationResult.Valid, ValidationResult.Invalid {
    record Valid() implements ValidationResult {}
    record Invalid(String reason) implements ValidationResult {}
}

// Usage with pattern matching
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
```

#### Benefits:
- **Type Safety**: Compiler knows all possible subtypes
- **Exhaustive Checking**: Switch expressions must handle all cases
- **Clear API**: Explicit about what implementations exist

---

## Sequenced Collections

### What are Sequenced Collections?
New interfaces that provide first/last element access and reversed views.

### Implementation Examples

#### Using First/Last (FilmService.java)
```java
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
```

#### Accessing Sequenced Elements (FilmService.java)
```java
var rates = films.stream()
        .map(Film::getRentalRate)
        .filter(rate -> rate != null)
        .sorted()
        .toList();

BigDecimal maxRate = rates.isEmpty() ? BigDecimal.ZERO : rates.getLast();
```

#### Benefits:
- **Intuitive API**: Clear intent with `getFirst()` and `getLast()`
- **Uniform**: Works across List, Deque, and SortedSet
- **Performant**: Direct access without iteration

---

## Text Blocks

### What are Text Blocks?
Multi-line string literals that preserve formatting.

### Implementation Examples

#### Formatted Output (FilmResourceV2.java)
```java
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
```

#### Benefits:
- **Readable**: No escape sequences for multi-line text
- **Maintainable**: Easier to edit and review
- **Natural**: Code looks like the output

---

## Enhanced instanceof

### What is Enhanced instanceof?
Pattern matching with instanceof eliminates casting.

### Implementation Examples

#### Type Pattern (FilmDTO.java)
```java
film.getRating() instanceof String s ? s : null
```

#### Traditional vs Enhanced:
```java
// Old way
if (obj instanceof String) {
    String s = (String) obj;
    return s.length();
}

// New way
if (obj instanceof String s) {
    return s.length();
}
```

#### Benefits:
- **Concise**: No explicit cast needed
- **Safer**: Pattern variable only in scope when type matches
- **Readable**: Intent is clearer

---

## Complete Feature Matrix

| Feature | Location | Description |
|---------|----------|-------------|
| Records | `FilmDTO`, `ActorDTO` | Immutable data carriers |
| Pattern Matching | `FilmService`, `FilmDTO` | Type testing with extraction |
| Switch Expressions | `FilmResourceV2`, `FilmDTO` | Value-returning switches |
| Sealed Interfaces | `FilmService` | Restricted type hierarchies |
| Sequenced Collections | `FilmService`, `PaginatedResult` | First/last element access |
| Text Blocks | `FilmResourceV2` | Multi-line strings |
| Enhanced instanceof | `FilmDTO` | Pattern matching instanceof |
| Compact Constructors | `FilmDTO`, `ActorDTO` | Validation in records |
| Guard Patterns | `FilmDTO`, `FilmService` | Conditional pattern matching |

---

## API Endpoints

### New V2 Endpoints (Java 25 Features)

```bash
# Get film with JSON response (using records)
GET /api/v2/films/{filmId}

# Get paginated films (using sequenced collections)
GET /api/v2/films?page=0&minLength=60&pageSize=20

# Search films (pattern matching categorization)
GET /api/v2/films/search?titlePrefix=A&minLength=60

# Update rental rates (sealed interface results)
PUT /api/v2/films/rental-rate?minLength=120&rate=5.99

# Get statistics (modern collection operations)
GET /api/v2/films/statistics?minLength=60

# Get formatted film (text blocks)
GET /api/v2/films/{filmId}/formatted?format=detailed
```

---

## Testing Java 25 Features

### FilmDTOTest.java
Tests demonstrate:
- Record creation and accessors
- Compact constructor validation
- Pattern matching in `getCategory()`
- Switch expression in `getFormattedDisplay()`
- Sequenced collection methods
- Record equality and immutability

### Example Test:
```java
@Test
void testGetCategory_usingPatternMatching() {
    assertEquals(FilmCategory.SHORT, createFilmDTO(30).getCategory());
    assertEquals(FilmCategory.FEATURE, createFilmDTO(90).getCategory());
    assertEquals(FilmCategory.LONG, createFilmDTO(150).getCategory());
    assertEquals(FilmCategory.EPIC, createFilmDTO(200).getCategory());
}
```

---

## Migration Guide

### From Old Code to Java 25

#### Before (Traditional Java):
```java
public class FilmDTO {
    private final Integer filmId;
    private final String title;
    // ... more fields

    public FilmDTO(Integer filmId, String title, ...) {
        this.filmId = filmId;
        this.title = title;
        // ...
    }

    public Integer getFilmId() { return filmId; }
    public String getTitle() { return title; }
    // ... more getters

    @Override
    public boolean equals(Object o) { /* ... */ }
    @Override
    public int hashCode() { /* ... */ }
    @Override
    public String toString() { /* ... */ }
}
```

#### After (Java 25):
```java
public record FilmDTO(
        Integer filmId,
        String title,
        // ... more fields
) {
    // Optional: compact constructor for validation
    public FilmDTO {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
    }
}
```

**Lines of code**: ~50+ → ~15

---

## Best Practices

1. **Use Records for DTOs**: Perfect for transferring data between layers
2. **Sealed Interfaces for Results**: Type-safe error handling
3. **Pattern Matching in Business Logic**: Clearer intent, safer code
4. **Sequenced Collections**: Use `getFirst()`/`getLast()` instead of index access
5. **Text Blocks for Templates**: More readable multi-line strings

---

## Performance Notes

- **Records**: Same performance as regular classes
- **Pattern Matching**: No runtime overhead
- **Switch Expressions**: Optimized by JVM (can use tableswitch/lookupswitch)
- **Sealed Types**: Enables better JVM optimizations
- **Sequenced Collections**: Same or better performance than manual implementations

---

## Further Reading

- [JEP 409: Sealed Classes](https://openjdk.org/jeps/409)
- [JEP 440: Record Patterns](https://openjdk.org/jeps/440)
- [JEP 441: Pattern Matching for switch](https://openjdk.org/jeps/441)
- [JEP 431: Sequenced Collections](https://openjdk.org/jeps/431)

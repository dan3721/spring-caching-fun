package com.example.pokemon.set;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Value
public class PokemonSet {
    String id;
    String name;
    String series;
    Integer printedTotal;
    Integer total;
    LocalDate releaseDate;
    LocalDateTime updatedAt;
    String symbolUrl;
    String logoUrl;
}

package com.example.pokemon.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pokemon_set")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PokemonSetEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String series;

    @Column(name = "printed_total")
    private Integer printedTotal;

    private Integer total;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "symbol_url", columnDefinition = "TEXT")
    private String symbolUrl;

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;
}

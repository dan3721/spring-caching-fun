package com.example.pokemon.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PokemonSetRepository extends JpaRepository<PokemonSetEntity, String> {

    List<PokemonSetEntity> findBySeriesOrderByReleaseDateAscNameAsc(String series);

    @Query("select distinct e.series from PokemonSetEntity e order by e.series")
    List<String> findDistinctSeriesOrdered();
}

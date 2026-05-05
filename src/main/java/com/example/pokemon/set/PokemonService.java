package com.example.pokemon.set;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.pokemon.jpa.PokemonSetRepository;

@Service
public class PokemonService {

    private final PokemonSetRepository pokemonSetRepository;
    private final PokemonSetMapper pokemonSetMapper;

    public PokemonService(PokemonSetRepository pokemonSetRepository, PokemonSetMapper pokemonSetMapper) {
        this.pokemonSetRepository = pokemonSetRepository;
        this.pokemonSetMapper = pokemonSetMapper;
    }

    @Cacheable(cacheNames = "pokemonSetsBySeries", key = "#series.toLowerCase()")
    public List<PokemonSet> getSets(String series) {
        return pokemonSetRepository.findBySeriesOrderByReleaseDateAscNameAsc(series).stream()
                .map(pokemonSetMapper::toPokemonSet)
                .toList();
    }

    @Cacheable(cacheNames = "pokemonSeriesNames")
    public List<String> getSeriesNames() {
        return pokemonSetRepository.findDistinctSeriesOrdered();
    }
}

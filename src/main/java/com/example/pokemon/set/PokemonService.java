package com.example.pokemon.set;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.example.pokemon.cache.CacheableD;
import com.example.pokemon.jpa.PokemonSetRepository;

@Service
public class PokemonService {

    private final PokemonSetRepository pokemonSetRepository;
    private final PokemonSetMapper pokemonSetMapper;

    public PokemonService(PokemonSetRepository pokemonSetRepository, PokemonSetMapper pokemonSetMapper) {
        this.pokemonSetRepository = pokemonSetRepository;
        this.pokemonSetMapper = pokemonSetMapper;
    }

    @Cacheable(
            cacheNames = "pokemonSetsBySeries",
            key = "#series.toLowerCase()",
            unless = "#result == null || #result.isEmpty()")
    public List<PokemonSet> getSets(String series) {
        return pokemonSetRepository.findBySeriesOrderByReleaseDateAscNameAsc(series).stream()
                .map(pokemonSetMapper::toPokemonSet)
                .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "pokemonSeriesNames")
    public List<String> getSeriesNames() {
        return pokemonSetRepository.findDistinctSeriesOrdered();
    }

    @CacheableD(cacheNames = "pokemonSeriesNamesD")
    public List<String> getSeriesNamesD() {
        return pokemonSetRepository.findDistinctSeriesOrdered();
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "pokemonSetsBySeries", key = "#pokemonSet.series.toLowerCase()"),
            @CacheEvict(cacheNames = "pokemonSeriesNames", allEntries = true),
            @CacheEvict(cacheNames = "pokemonSeriesNamesD", allEntries = true)
    })
    public PokemonSet createSet(PokemonSet pokemonSet) {
        return pokemonSetMapper.toPokemonSet(pokemonSetRepository.save(pokemonSetMapper.toEntity(pokemonSet)));
    }
}

package com.example.pokemon.set;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping("/sets")
    public List<PokemonSet> getSets(@RequestParam String series) {
        return pokemonService.getSets(series);
    }

    @GetMapping("/series")
    public List<String> getSeriesNames() {
        return pokemonService.getSeriesNames();
    }
}

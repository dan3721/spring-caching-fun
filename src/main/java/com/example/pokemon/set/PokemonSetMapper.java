package com.example.pokemon.set;

import org.mapstruct.Mapper;

import com.example.pokemon.jpa.PokemonSetEntity;

@Mapper(componentModel = "spring")
public interface PokemonSetMapper {
    PokemonSet toPokemonSet(PokemonSetEntity entity);
}

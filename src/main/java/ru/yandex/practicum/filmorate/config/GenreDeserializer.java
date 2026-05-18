package ru.yandex.practicum.filmorate.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.GenreOfFilm;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class GenreDeserializer extends JsonDeserializer<Set<GenreOfFilm>> {

    @Override
    public Set<GenreOfFilm> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        Set<GenreOfFilm> genres = new LinkedHashSet<>();

        if (node.isArray()) {
            for (JsonNode genreNode : node) {
                GenreOfFilm genre = null;

                if (genreNode.has("id")) {
                    int id = genreNode.get("id").asInt();
                    genre = GenreMapper.mapIdToGenre(id);
                } else if (genreNode.isInt()) {
                    genre = GenreMapper.mapIdToGenre(genreNode.asInt());
                } else if (genreNode.isTextual()) {
                    String name = genreNode.asText();
                    genre = GenreOfFilm.valueOf(name);
                }

                if (genre != null) {
                    genres.add(genre);
                }
            }
        }

        return genres;
    }
}
package ru.yandex.practicum.filmorate.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ru.yandex.practicum.filmorate.mapper.MapMapper;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.io.IOException;

public class MpaDeserializer extends JsonDeserializer<MotionPictureAssociation> {

    @Override
    public MotionPictureAssociation deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        if (node.has("id")) {
            Integer id = node.get("id").asInt();
            return MapMapper.mapIdToMpa(id);
        }

        if (node.isInt()) {
            return MapMapper.mapIdToMpa(node.asInt());
        }

        String value = node.asText();
        if (value != null && !value.isEmpty()) {
            try {
                Integer id = Integer.parseInt(value);
                return MapMapper.mapIdToMpa(id);
            } catch (NumberFormatException e) {
                try {
                    return MotionPictureAssociation.valueOf(value);
                } catch (IllegalArgumentException ex) {
                    return null;
                }
            }
        }

        return null;
    }
}
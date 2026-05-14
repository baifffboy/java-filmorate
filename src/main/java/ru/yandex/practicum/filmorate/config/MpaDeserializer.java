package ru.yandex.practicum.filmorate.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.io.IOException;

public class MpaDeserializer extends JsonDeserializer<MotionPictureAssociation> {

    @Override
    public MotionPictureAssociation deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        // Если пришёл объект с полем id
        if (node.has("id")) {
            int id = node.get("id").asInt();
            return mapIdToMpa(id);
        }

        // Если пришла строка
        String value = node.asText();
        if (value != null && !value.isEmpty()) {
            try {
                int id = Integer.parseInt(value);
                return mapIdToMpa(id);
            } catch (NumberFormatException e) {
                return MotionPictureAssociation.valueOf(value);
            }
        }

        return null;
    }

    private MotionPictureAssociation mapIdToMpa(int id) {
        switch (id) {
            case 1:
                return MotionPictureAssociation.G;
            case 2:
                return MotionPictureAssociation.PG;
            case 3:
                return MotionPictureAssociation.PG13;
            case 4:
                return MotionPictureAssociation.R;
            case 5:
                return MotionPictureAssociation.NC17;
            default:
                return null;
        }
    }
}

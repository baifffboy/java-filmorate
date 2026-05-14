package ru.yandex.practicum.filmorate.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.filmorate.model.MotionPictureAssociation;

import java.util.Set;

@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule filmorateModule() {
        SimpleModule module = new SimpleModule();

        module.addDeserializer(MotionPictureAssociation.class, new MpaDeserializer());
        module.addDeserializer(Set.class, new GenreDeserializer());

        return module;
    }
}

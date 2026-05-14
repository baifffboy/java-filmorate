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

        // Регистрируем десериализатор для MPA
        module.addDeserializer(MotionPictureAssociation.class, new MpaDeserializer());

        // Регистрируем десериализатор для жанров
        module.addDeserializer(Set.class, new GenreDeserializer());

        return module;
    }
}

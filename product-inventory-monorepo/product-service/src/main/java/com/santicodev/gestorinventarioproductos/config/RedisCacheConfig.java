package com.santicodev.gestorinventarioproductos.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;

@Configuration
@EnableCaching // Asegura que la capacidad de caché esté habilitada
public class RedisCacheConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60)) // Tiempo de vida por defecto para las entradas de caché: 60 minutos
                .disableCachingNullValues() // No cachear valores nulos (opcional, pero común)
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
                // Usa Jackson para serializar los objetos a JSON en Redis.
                // Esto hace que los datos en Redis sean legibles y interoperables.
    }

    // Si necesitas caches con configuraciones TTL diferentes:
    // @Bean
    // public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
    //     return (builder) -> builder
    //             .withCacheConfiguration("productosCache",
    //                     RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)))
    //             .withCacheConfiguration("categoriasCache",
    //                     RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));
    // }
}

package com.santicodev.gestorinventarioproductos.integration.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.redis.testcontainers.RedisContainer;

// Necesitarás inyectar esto para limpiar Redis
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {

    // Contenedores de Testcontainers
    @Container
    protected static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    protected static RedisContainer redisContainer =
            new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag("6.2.6"));

    // Inyectamos RedisTemplate para poder limpiar la cache de Redis
    // 'required = false' en caso de que algún test no use Redis
    @Autowired(required = false)
    protected RedisTemplate<String, Object> redisTemplate;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        // Propiedades para PostgreSQL
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Propiedades para Redis
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(redisContainer.getFirstMappedPort()));
    }

    // Metodo para limpiar el estado antes de CADA prueba
    // Es crucial para la independencia de las pruebas.
    @BeforeEach
    protected void setupBase() {
        // Limpiar Redis antes de cada prueba, si RedisTemplate está disponible.
        // Asegúrate de que esta limpieza sea segura y no afecte a entornos no-test.
        if (redisTemplate != null && redisTemplate.getConnectionFactory() != null) {
            try {
                redisTemplate.getConnectionFactory().getConnection().flushDb();
            } catch (Exception e) {
                System.err.println("Error flushing Redis DB: " + e.getMessage());
                // Considera si este error debe fallar la prueba o solo advertir.
            }
        }
        // Nota: La limpieza de la base de datos SQL se hará en las clases hijas
        // que inyecten sus respectivos repositorios para un control más granular.
    }
}

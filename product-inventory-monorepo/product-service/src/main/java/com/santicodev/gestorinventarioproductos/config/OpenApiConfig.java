package com.santicodev.gestorinventarioproductos.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "SantiCodev",
                        email = "contact@santicodev.com", // Reemplaza con tu email
                        url = "https://www.santicodev.com" // Reemplaza con tu URL
                ),
                description = "OpenAPI documentation for Spring Boot Inventory API",
                title = "Inventory Management API - Secure Version",
                version = "1.0",
                license = @License(
                        name = "Licencia Propia", // O una licencia de código abierto
                        url = "https://www.santicodev.com/license" // URL de la licencia
                ),
                termsOfService = "Términos de servicio de SantiCodev"
        ),
        servers = {
                @Server(
                        description = "Entorno Docker",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "Entorno Local",
                        url = "http://localhost:8081"
                )
                // Puedes añadir más servidores (ej. desarrollo, producción)
                // @Server(
                //     description = "Entorno de Desarrollo",
                //     url = "https://dev.api.yourdomain.com"
                // )
        },
        // *** AÑADE ESTA ANOTACIÓN PARA APLICAR EL ESQUEMA DE SEGURIDAD GLOBALMENTE ***
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth", // Nombre de este esquema de seguridad (lo usarás para referenciarlo)
        description = "JWT authentication",
        type = SecuritySchemeType.HTTP, // Tipo de esquema (HTTP para Bearer)
        bearerFormat = "JWT", // Formato del token
        scheme = "bearer", // Esquema HTTP (Bearer)
        in = SecuritySchemeIn.HEADER // Dónde se envía el token (en la cabecera)
)
@Configuration // Marca la clase como una configuración de Spring
public class OpenApiConfig {
    // No se necesita código dentro de la clase para esta configuración básica de OpenAPI.
    // Las anotaciones hacen todo el trabajo.
}

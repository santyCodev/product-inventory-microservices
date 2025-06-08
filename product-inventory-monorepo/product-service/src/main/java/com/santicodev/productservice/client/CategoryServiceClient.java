package com.santicodev.productservice.client;

import com.santicodev.commondomainlib.infraestructure.dto.CategoryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

// @FeignClient: Indica que esta interfaz es un cliente Feign.
// "category-service": Es el nombre lógico del microservicio de categorías (se resolverá con Eureka).
//                  Por ahora, si no tienes Eureka, Feign intentará usarlo como nombre de host si se configura un URL.
// path: Prefijo de URL para todas las peticiones de este cliente.
@FeignClient(name = "category-service", path = "/api/v1categories")
public interface CategoryServiceClient {

    // Define el endpoint para obtener una categoría por ID.
    // La firma del metodo y las anotaciones (@GetMapping, @PathVariable)
    // deben coincidir con la API que expondrá el Category Service.
    @GetMapping("/{id}")
    Optional<CategoryDTO> getCategoryById(@PathVariable("id") Long id);

    // Puedes añadir otros métodos aquí si Product Service necesita interactuar con Category Service
    // de otras formas (ej., obtener todas las categorías, crear categorías, etc.).
}

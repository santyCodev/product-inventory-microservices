package com.santicodev.gestorinventarioproductos.integration.category;

import com.fasterxml.jackson.databind.ObjectMapper; // Para convertir objetos Java a JSON y viceversa
import com.santicodev.gestorinventarioproductos.category.domain.Category;
import com.santicodev.gestorinventarioproductos.category.infraestructure.repository.CategoryRepository;
import com.santicodev.gestorinventarioproductos.integration.config.BaseIntegrationTest;
import com.santicodev.gestorinventarioproductos.common.infraestructure.dto.CategoryDTO; // Tu DTO de categoría
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType; // Para especificar el tipo de contenido (e.g., application/json)
import org.springframework.security.test.context.support.WithMockUser; // Para simular usuarios autenticados
import org.springframework.test.web.servlet.MockMvc; // Objeto principal para realizar llamadas HTTP simuladas
import org.springframework.test.web.servlet.setup.MockMvcBuilders; // Para construir la instancia de MockMvc
import org.springframework.web.context.WebApplicationContext; // El contexto de la aplicación web de Spring

import static org.hamcrest.Matchers.hasSize; // Aserción para verificar el tamaño de colecciones JSON
import static org.hamcrest.Matchers.is; // Aserción para verificar la igualdad
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; // Importa estáticamente los métodos HTTP (get, post, put, delete)
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // Importa estáticamente los métodos para verificar las respuestas
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity; // Para integrar Spring Security con MockMvc
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Pruebas de integración para la capa de controlador (API REST) de Categorías.
 * Simula peticiones HTTP de extremo a extremo, verificando la validación de DTOs,
 * la seguridad y las respuestas HTTP.
 */
@DisplayName("CategoryController Integration Tests")
class CategoryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext; // Contexto de la aplicación web, necesario para MockMvc

    private MockMvc mockMvc; // Objeto para simular peticiones HTTP

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos Java a JSON y viceversa

    @Autowired
    private CategoryRepository categoryRepository; // Para preparar datos y verificar el estado de la DB directamente

    /**
     * Configuración inicial antes de cada prueba.
     * Limpia la base de datos y configura MockMvc con Spring Security.
     */
    @Override
    @BeforeEach
    public void setupBase() {
        super.setupBase(); // Llama al metodo de limpieza de la clase padre (para Redis)
        categoryRepository.deleteAll(); // Limpia la tabla de categorías en PostgreSQL antes de cada test.
        // Es crucial para la independencia de los tests.
        // Construye MockMvc y aplica la configuración de Spring Security.
        // Esto permite que @WithMockUser funcione y que los filtros de seguridad procesen las peticiones simuladas.
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    // --- Pruebas para GET /api/categories ---
    @Test
    @DisplayName("GET /api/v1/categories debería retornar todas las categorías (usuario autenticado)")
    // Simula un usuario autenticado con el rol USER
    @WithMockUser(username = "user", roles = {"USER"})
    void getAllCategories_authenticatedUser_shouldReturnCategories() throws Exception {
        // ARRANGE: Preparar algunas categorías en la base de datos.
        categoryRepository.save(new Category(null,"Libros", "Medios impresos y digitales."));
        categoryRepository.save(new Category(null,"Películas", "Entretenimiento audiovisual."));

        // ACT & ASSERT: Realizar una petición GET y verificar la respuesta.
        mockMvc.perform(get("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)) // Especifica que el cliente envía JSON
                .andExpect(status().isOk()) // Espera un código de estado HTTP 200 OK
                .andExpect(jsonPath("$", hasSize(2))) // Espera que el JSON de respuesta sea un array de tamaño 2
                .andExpect(jsonPath("$[0].name", is("Libros"))) // Verifica el nombre de la primera categoría (orden puede variar)
                .andExpect(jsonPath("$[1].name", is("Películas"))); // Verifica el nombre de la segunda categoría
    }

    @Test
    @DisplayName("GET /api/v1/categories debería retornar 401 Unauthorized para acceso no autenticado")
    void getAllCategories_unauthenticatedUser_shouldReturnUnauthorized() throws Exception {
        // ACT & ASSERT: Realizar una petición GET sin autenticación.
        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Espera un código de estado HTTP 401 Unauthorized
    }

    // --- Pruebas para GET /api/categories/{id} ---
    @Test
    @DisplayName("GET /api/v1/categories/{id} debería retornar una categoría por ID")
    @WithMockUser(username = "user", roles = {"USER"})
    void getCategoryById_shouldReturnCategory() throws Exception {
        // ARRANGE: Guardar una categoría para poder buscarla por ID.
        Category existingCategory = categoryRepository.save(new Category(null,"Videojuegos", "Videojuegos y consolas."));

        // ACT & ASSERT: Realizar una petición GET por ID.
        mockMvc.perform(get("/api/v1/categories/{id}", existingCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Videojuegos"))); // Verifica el nombre de la categoría encontrada
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} debería retornar 404 Not Found para categoría inexistente")
    @WithMockUser(username = "user", roles = {"USER"})
    void getCategoryById_nonExistent_shouldReturnNotFound() throws Exception {
        // ACT & ASSERT: Intentar buscar una categoría con un ID que no existe.
        mockMvc.perform(get("/api/v1/categories/{id}", 999L) // ID que no existe
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Espera un código de estado HTTP 404 Not Found
    }

    // --- Pruebas para POST /api/categories ---
    @Test
    @DisplayName("POST /api/v1/categories debería crear una nueva categoría (usuario ADMIN autenticado)")
    @WithMockUser(username = "admin", roles = {"ADMIN"}) // Simula un usuario ADMIN (con permisos de creación)
    void createCategory_authenticatedAdmin_shouldCreateCategory() throws Exception {
        // ARRANGE: Crear un CategoryDTO con datos válidos.
        CategoryDTO newCategoryDTO = new CategoryDTO(null, "Nueva Categoría", "Descripción de la nueva categoría.");

        // ACT & ASSERT: Realizar una petición POST para crear la categoría.
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON) // Especifica que el cuerpo es JSON
                .content(objectMapper.writeValueAsString(newCategoryDTO))) // Convierte el DTO a String JSON
                .andExpect(status().isCreated()) // Espera un código de estado HTTP 201 Created
                .andExpect(jsonPath("$.id").exists()) // Verifica que el ID de la categoría se haya generado
                .andExpect(jsonPath("$.name", is("Nueva Categoría"))); // Verifica el nombre en la respuesta

        // Verificación directa en la base de datos para confirmar la persistencia.
        assertThat(categoryRepository.count()).isEqualTo(1); // Debería haber una categoría en la DB
        assertThat(categoryRepository.findAll().get(0).getName()).isEqualTo("Nueva Categoría");
    }

    @Test
    @DisplayName("POST /api/v1/categories debería retornar 403 Forbidden para usuario USER autenticado")
    @WithMockUser(username = "user", roles = {"USER"}) // Simula un usuario USER (sin permisos de creación)
    void createCategory_authenticatedUser_shouldReturnForbidden() throws Exception {
        // ARRANGE: Crear un DTO de categoría para intentar crear.
        CategoryDTO newCategoryDTO = new CategoryDTO(null, "Categoría Prohibida", "Intento de creación por usuario normal.");

        // ACT & ASSERT: Realizar la petición POST.
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDTO)))
                .andExpect(status().isForbidden()); // Espera un código de estado HTTP 403 Forbidden
    }

    // --- Pruebas de Validación de DTO para POST /api/categories ---
    @Test
    @DisplayName("POST /api/v1/categories debería retornar 400 Bad Request para nombre de categoría en blanco")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createCategory_invalidNameBlank_shouldReturnBadRequest() throws Exception {
        // ARRANGE: CategoryDTO con nombre vacío o en blanco (@NotBlank).
        CategoryDTO invalidCategoryDTO = new CategoryDTO(null, "   ", "Descripción inválida");

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategoryDTO)))
                .andExpect(status().isBadRequest()) // Espera 400 Bad Request
                .andExpect(jsonPath("$.message").exists()); // Verifica que haya un mensaje de error en la respuesta
    }

    @Test
    @DisplayName("POST /api/v1/categories debería retornar 400 Bad Request para nombre de categoría demasiado corto")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createCategory_nameTooShort_shouldReturnBadRequest() throws Exception {
        // ARRANGE: CategoryDTO con nombre con menos de 3 caracteres (@Size(min=3)).
        CategoryDTO invalidCategoryDTO = new CategoryDTO(null, "Ab", "Nombre demasiado corto");

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategoryDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/categories debería retornar 400 Bad Request para descripción de categoría demasiado larga")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createCategory_descriptionTooLong_shouldReturnBadRequest() throws Exception {
        // ARRANGE: CategoryDTO con descripción que excede 255 caracteres (@Size(max=255)).
        String longDescription = "a".repeat(256); // 256 'a's
        CategoryDTO invalidCategoryDTO = new CategoryDTO(null, "Categoría Larga", longDescription);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategoryDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // --- Pruebas para PUT /api/categories/{id} ---
    @Test
    @DisplayName("PUT /api/v1/categories/{id} debería actualizar una categoría (usuario ADMIN autenticado)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateCategory_authenticatedAdmin_shouldUpdateCategory() throws Exception {
        // ARRANGE: Guardar una categoría existente para actualizar.
        Category existingCategory = categoryRepository.save(new Category(null,"Antigua Cat", "Antigua Desc"));

        // Crear un CategoryDTO con los datos actualizados.
        CategoryDTO updatedCategoryDTO = new CategoryDTO(existingCategory.getId(), "Categoría Actualizada", "Descripción Actualizada");

        // ACT & ASSERT: Realizar una petición PUT.
        mockMvc.perform(put("/api/v1/categories/{id}", existingCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCategoryDTO)))
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.name", is("Categoría Actualizada")))
                .andExpect(jsonPath("$.description", is("Descripción Actualizada")));

        // Verificación directa en la base de datos.
        Category updatedInDb = categoryRepository.findById(existingCategory.getId()).orElseThrow();
        assertThat(updatedInDb.getName()).isEqualTo("Categoría Actualizada");
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} debería retornar 400 Bad Request para nombre inválido al actualizar")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateCategory_invalidName_shouldReturnBadRequest() throws Exception {
        // ARRANGE: Categoría existente y un DTO de actualización con nombre inválido.
        Category existingCategory = categoryRepository.save(new Category(null, "Nombre Válido", "Desc Válida"));
        CategoryDTO invalidUpdateDTO = new CategoryDTO(existingCategory.getId(), "", "Descripción de actualización inválida");

        // ACT & ASSERT
        mockMvc.perform(put("/api/v1/categories/{id}", existingCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} debería retornar 404 Not Found si la categoría a actualizar no existe")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateCategory_nonExistent_shouldReturnNotFound() throws Exception {
        // ARRANGE: DTO de actualización para un ID que no existe.
        CategoryDTO nonExistentCategoryDTO = new CategoryDTO(999L, "No Existe", "Descripción");

        // ACT & ASSERT
        mockMvc.perform(put("/api/v1/categories/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentCategoryDTO)))
                .andExpect(status().isNotFound());
    }

    // --- Pruebas para DELETE /api/categories/{id} ---
    @Test
    @DisplayName("DELETE /api/v1/categories/{id} debería eliminar una categoría (usuario ADMIN autenticado)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteCategory_authenticatedAdmin_shouldDeleteCategory() throws Exception {
        // ARRANGE: Guardar una categoría que será eliminada.
        Category categoryToDelete = categoryRepository.save(new Category(null,"Para Borrar", "Categoría efímera"));

        // ACT & ASSERT: Realizar una petición DELETE.
        mockMvc.perform(delete("/api/v1/categories/{id}", categoryToDelete.getId()))
                .andExpect(status().isNoContent()); // Espera 204 No Content

        // Verificación directa en la base de datos.
        assertFalse(categoryRepository.existsById(categoryToDelete.getId()),
                "La categoría debería haber sido eliminada de la base de datos.");
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} debería retornar 404 Not Found si la categoría a eliminar no existe")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteCategory_nonExistent_shouldReturnNotFound() throws Exception {
        // ACT & ASSERT
        mockMvc.perform(delete("/api/v1/categories/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} debería retornar 403 Forbidden para usuario USER autenticado")
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteCategory_authenticatedUser_shouldReturnForbidden() throws Exception {
        // ARRANGE: Guardar una categoría que no debería ser eliminada por un USER.
        Category existingCategory = categoryRepository.save(new Category(null,"Categoría Protegida", "No se debería borrar"));

        // ACT & ASSERT
        mockMvc.perform(delete("/api/v1/categories/{id}", existingCategory.getId()))
                .andExpect(status().isForbidden()); // Espera 403 Forbidden
    }
}

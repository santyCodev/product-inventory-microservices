package com.santicodev.gestorinventarioproductos.integration.category;

import com.santicodev.gestorinventarioproductos.category.application.service.CategoryService;
import com.santicodev.gestorinventarioproductos.category.domain.Category;
import com.santicodev.gestorinventarioproductos.category.infraestructure.repository.CategoryRepository;
import com.santicodev.gestorinventarioproductos.integration.config.BaseIntegrationTest;
import com.santicodev.gestorinventarioproductos.common.domain.exception.ResourceNotFoundException;
import com.santicodev.gestorinventarioproductos.common.infraestructure.dto.CategoryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para la capa de servicio de Categorías.
 * Extiende BaseIntegrationTest para obtener la configuración de Testcontainers y Spring Boot.
 * Se enfoca en la lógica de negocio y la interacción con la base de datos a través del repositorio.
 */
@DisplayName("CategoryService Integration Tests")
public class CategoryServiceIntegrationTest extends BaseIntegrationTest {

    // Inyecta el servicio que vamos a probar.
    @Autowired
    private CategoryService categoryService;

    // Inyecta el repositorio para preparar datos y verificar resultados directamente en la DB.
    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Este metodo se ejecuta antes de cada prueba.
     * Garantiza un estado limpio de la base de datos y Redis para cada test individual.
     */
    @Override
    @BeforeEach
    public void setupBase() {
        // Llama al metodo de la clase padre para limpiar Redis (si es aplicable a este servicio)
        super.setupBase();
        // **CRÍTICO:** Limpia la tabla de categorías en PostgreSQL antes de cada test.
        categoryRepository.deleteAll();
        // Esto asegura la independencia de los tests.
    }

    @Test
    @DisplayName("Debería guardar una nueva categoría correctamente y verificar su persistencia")
    void shouldSaveNewCategoryCorrectlyAndVerifyPersistence() {
        // ARRANGE (Preparación): Crear un DTO de categoria con datos válidos.
        CategoryDTO newCategoryDTO = new CategoryDTO(
                null,
                "Electronica",
                "Dispositivos, gadgets y accesorios electrónicos.");

        // ACT (Acción): Llamar al metodo del servicio que se va a probar.
        CategoryDTO savedCategory = categoryService.createCategory(newCategoryDTO);

        // ASSERT (Verificación): Confirmar que la acción produjo el resultado esperado.
        // 1. Verificar que el objeto devuelto por el servicio no es nulo y tiene un ID.
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.id()).isNotNull();
        // 2. Verificar que los datos guardados son los esperados.
        assertThat(savedCategory.name()).isEqualTo("Electronica");
        assertThat(savedCategory.description()).isEqualTo("Dispositivos, gadgets y accesorios electrónicos.");

        // 3. **Verificación directa en la base de datos:** Asegurarse de que el dato realmente se persistió.
        Optional<Category> foundInDb = categoryRepository.findById(savedCategory.id());
        assertTrue(foundInDb.isPresent(), "La categoría guardada debería existir en la base de datos.");
        assertThat(foundInDb.get().getName()).isEqualTo("Electronica");
        assertThat(foundInDb.get().getDescription()).isEqualTo("Dispositivos, gadgets y accesorios electrónicos.");
    }

    @Test
    @DisplayName("Debería obtener una categoría por su ID")
    void shouldGetCategoryById() {
        // ARRANGE: Guardar una categoría para poder recuperarla.
        Category existingCategory = new Category(null,"Libros", "Títulos de ficción y no ficción");
        categoryRepository.save(existingCategory);

        // ACT: Intentar obtener la categoría por su ID.
        CategoryDTO foundCategory = categoryService.getCategoryById(existingCategory.getId());

        // ASSERT: Verificar que la categoría obtenida es la correcta.
        assertThat(foundCategory).isNotNull();
        assertThat(foundCategory.id()).isEqualTo(existingCategory.getId());
        assertThat(foundCategory.name()).isEqualTo("Libros");
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si la categoría no se encuentra por ID")
    void shouldThrowNotFoundExceptionWhenCategoryNotFound() {
        // ACT & ASSERT: Intentar obtener una categoría con un ID que sabemos que no existe.
        // Se espera que se lance una ResourceNotFoundException.
        assertThrows(ResourceNotFoundException.class,() ->
            categoryService.getCategoryById(999L),
                "Se esperaba ResourceNotFoundException para una categoría no encontrada");
    }

    @Test
    @DisplayName("Debería obtener todas las categorías existentes")
    void shouldGetAllCategories() {
        // ARRANGE: Guardar varias categorías.
        categoryRepository.save(new Category(null,"Ropa", "Todo tipo de prendas de vestir"));
        categoryRepository.save(new Category(null,"Hogar", "Artículos para el hogar"));

        // ACT: Obtener todas las categorías.
        List<CategoryDTO> categories = categoryService.getAllCategories();

        // ASSERT: Verificar que se recuperaron las categorías correctas y la cantidad.
        assertThat(categories).isNotNull().hasSize(2);
        assertThat(categories)
                .extracting(CategoryDTO::name) // Extrae solo los nombres de las categorías
                .containsExactlyInAnyOrder("Ropa", "Hogar"); // Verifica que contengan estos nombres, sin importar el orden
    }

    @Test
    @DisplayName("Debería actualizar una categoría existente y verificar la persistencia")
    void shouldUpdateCategory() {
        // ARRANGE: Guardar una categoría existente para actualizarla.
        Category existingCategory = new Category(null,"Nombre Antiguo", "Descripción Antigua");
        categoryRepository.save(existingCategory);

        // Crear una Category con los nuevos detalles para la actualización.
        CategoryDTO updatedDetails = new CategoryDTO(
                null,
                "Nuevo Nombre de Categoría",
                "Descripción actualizada para la categoría");

        // ACT: Llamar al metodo de actualización del servicio.
        CategoryDTO updatedCategory = categoryService.updateCategory(existingCategory.getId(), updatedDetails);

        // ASSERT: Verificar que el objeto devuelto refleja la actualización.
        assertThat(updatedCategory).isNotNull();
        assertThat(updatedCategory.id()).isEqualTo(existingCategory.getId()); // El ID no debe cambiar
        assertThat(updatedCategory.name()).isEqualTo("Nuevo Nombre de Categoría");
        assertThat(updatedCategory.description()).isEqualTo("Descripción actualizada para la categoría");

        // Verificación directa en la base de datos para confirmar la actualización.
        Optional<Category> foundInDb = categoryRepository.findById(existingCategory.getId());
        assertTrue(foundInDb.isPresent(), "La categoría debería seguir existiendo en la base de datos.");
        assertThat(foundInDb.get().getName()).isEqualTo("Nuevo Nombre de Categoría");
        assertThat(foundInDb.get().getDescription()).isEqualTo("Descripción actualizada para la categoría");
    }

    @Test
    @DisplayName("Debería eliminar una categoría por ID")
    void shouldDeleteCategory() {
        // ARRANGE: Guardar una categoría que será eliminada.
        Category categoryToDelete = new Category(null,"Para Eliminar", "Esta categoría será borrada");
        categoryRepository.save(categoryToDelete);

        // ACT: Llamar al método de eliminación del servicio.
        categoryService.deleteCategory(categoryToDelete.getId());

        // ASSERT: Verificar que la categoría ya no existe en la base de datos.
        assertFalse(categoryRepository.existsById(categoryToDelete.getId()),
                "La categoría debería haber sido eliminada de la base de datos.");
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException al intentar actualizar una categoría inexistente")
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistentCategory() {
        // ARRANGE: Datos para la actualización, pero para un ID no existente.
        CategoryDTO updateData = new CategoryDTO(
                99L,
                "Update Name",
                "Update Desc");
        // ACT & ASSERT: Se espera una ResourceNotFoundException.
        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(999L, updateData),
                "Se esperaba ResourceNotFoundException al intentar actualizar una categoría que no existe.");
    }
}

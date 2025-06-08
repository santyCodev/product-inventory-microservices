//package com.santicodev.categoryservice.application.service;
//
//import com.santicodev.commondomainlib.domain.exception.DuplicateResourceException;
//import com.santicodev.commondomainlib.domain.exception.ResourceNotFoundException;
//import com.santicodev.commondomainlib.infraestructure.dto.CategoryDTO;
//import com.santicodev.commondomainlib.infraestructure.dto.CategoryPartialUpdateDTO;
//import com.santicodev.productservice.category.domain.Category;
//import com.santicodev.productservice.category.infraestructure.repository.CategoryRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//// 1. Anotación: Integra Mockito con JUnit 5, permitiendo el uso de @Mock y @InjectMocks.
//@ExtendWith(MockitoExtension.class)
//// 2. Anotación: Proporciona un nombre más descriptivo para el conjunto de pruebas.
//@DisplayName("CategoryService Unit Tests")
//public class CategoryServiceTest {
//
//    // 3. Anotación: Crea una instancia simulada (mock) de CategoryRepository.
//    // Cuando el código llame a métodos en 'categoryRepository', se usará este mock.
//    @Mock
//    private CategoryRepository categoryRepository;
//
//    // 4. Anotación: Inyecta los mocks (como categoryRepository) en una instancia real de CategoryService.
//    @InjectMocks
//    private CategoryService categoryService;
//
//    private Category category1;
//    private CategoryDTO categoryDTO1;
//
//    // 5. Anotación: Este método se ejecuta antes de cada método de prueba (@Test). Útil para inicializar objetos comunes.
//    @BeforeEach
//    void setUp() {
//        category1 = new Category(1L, "Electronics", "Devices and gadgets");
//        categoryDTO1 = new CategoryDTO(1L, "Electronics", "Devices and gadgets");
//    }
//
//    // 6. Anotación: Marca este método como un método de prueba.
//    @Test
//    @DisplayName("Should create a new category successfully")
//    void shouldCreateCategorySuccessfully() {
//        // Given (Dado): Configuración de los mocks y datos de entrada.
//
//        // Cuando categoryRepository.existsByName es llamado con "Electronics", debe retornar false.
//        when(categoryRepository.existsByName("Electronics")).thenReturn(false);
//        // Cuando categoryRepository.save es llamado con cualquier objeto Category, debe retornar category1.
//        when(categoryRepository.save(any(Category.class))).thenReturn(category1);
//
//        // When (Cuando): Ejecución del método que estamos probando.
//        CategoryDTO result = categoryService.createCategory(categoryDTO1);
//
//        // Then (Entonces): Verificación del resultado y de las interacciones con los mocks.
//        assertNotNull(result); // El resultado no debe ser nulo.
//        assertEquals(categoryDTO1.name(), result.name()); // El nombre del DTO de resultado debe coincidir.
//        assertEquals(categoryDTO1.description(), result.description());
//        // 8. Verificación Mockito: Asegura que existsByName fue llamado una vez con "Electronics".
//        verify(categoryRepository, times(1)).existsByName("Electronics");
//        // Asegura que save fue llamado una vez con cualquier instancia de Category.
//        verify(categoryRepository, times(1)).save(any(Category.class));
//    }
//
//    @Test
//    @DisplayName("Should throw DuplicateResourceException when creating category with existing name")
//    void shouldThrowDuplicateResourceExceptionOnCreateExistingName() {
//        // Given
//        when(categoryRepository.existsByName("Electronics")).thenReturn(true);
//
//        // When & Then (Verifica que se lanza la excepción esperada)
//        // 9. assertThrows: Verifica que el código dentro del lambda lanza una excepción de un tipo específico.
//        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
//            categoryService.createCategory(categoryDTO1);
//        });
//        assertEquals("La categoría con el nombre 'Electronics' ya existe.", exception.getMessage());
//        verify(categoryRepository, times(1)).existsByName("Electronics");
//        verify(categoryRepository, never()).save(any(Category.class)); // 10. never(): Verifica que save nunca fue llamado.
//    }
//
//    @Test
//    @DisplayName("Should return all categories successfully")
//    void shouldReturnAllCategoriesSuccessfully() {
//        // Given
//        Category category2 = new Category(2L, "Clothes", "Apparel");
//        List<Category> categories = Arrays.asList(category1, category2);
//        when(categoryRepository.findAll()).thenReturn(categories);
//
//        // When
//        List<CategoryDTO> result = categoryService.getAllCategories();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals("Electronics", result.get(0).name());
//        assertEquals("Clothes", result.get(1).name());
//        verify(categoryRepository, times(1)).findAll();
//    }
//
//    @Test
//    @DisplayName("Should return category by ID successfully")
//    void shouldReturnCategoryByIdSuccessfully() {
//        // Given
//        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1)); // Retorna un Optional con la categoría.
//
//        // When
//        CategoryDTO result = categoryService.getCategoryById(1L);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("Electronics", result.name());
//        verify(categoryRepository, times(1)).findById(1L);
//    }
//
//    @Test
//    @DisplayName("Should throw ResourceNotFoundException when getting category by non-existing ID")
//    void shouldThrowResourceNotFoundExceptionOnGetNonExistingId() {
//        // Given
//        when(categoryRepository.findById(99L)).thenReturn(Optional.empty()); // Retorna un Optional vacío.
//
//        // When & Then
//        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
//            categoryService.getCategoryById(99L);
//        });
//        assertEquals("Categoría no encontrada con ID: 99", exception.getMessage());
//        verify(categoryRepository, times(1)).findById(99L);
//    }
//
//    @Test
//    @DisplayName("Should update an existing category successfully")
//    void shouldUpdateCategorySuccessfully() {
//        // Given
//        CategoryDTO updatedCategoryDTO = new CategoryDTO(1L, "Updated Electronics", "New description");
//        Category updatedCategoryEntity = new Category(1L, "Updated Electronics", "New description");
//
//        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
//        when(categoryRepository.existsByName("Updated Electronics")).thenReturn(false);
//        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategoryEntity);
//
//        // When
//        CategoryDTO result = categoryService.updateCategory(1L, updatedCategoryDTO);
//
//        // Then
//        assertNotNull(result);
//        assertEquals("Updated Electronics", result.name());
//        assertEquals("New description", result.description());
//        verify(categoryRepository, times(1)).findById(1L);
//        verify(categoryRepository, times(1)).existsByName("Updated Electronics");
//        verify(categoryRepository, times(1)).save(any(Category.class));
//    }
//
//    @Test
//    @DisplayName("Should throw ResourceNotFoundException when updating non-existing category")
//    void shouldThrowResourceNotFoundExceptionOnUpdateNonExisting() {
//        // Given
//        CategoryDTO updatedCategoryDTO = new CategoryDTO(99L, "Non Existent", "Desc");
//        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
//
//        // When & Then
//        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
//            categoryService.updateCategory(99L, updatedCategoryDTO);
//        });
//        assertEquals("Categoría no encontrada con ID: 99", exception.getMessage());
//        verify(categoryRepository, times(1)).findById(99L);
//        verify(categoryRepository, never()).save(any(Category.class));
//    }
//
//    @Test
//    @DisplayName("Should delete an existing category successfully")
//    void shouldDeleteCategorySuccessfully() {
//        // Given
//        when(categoryRepository.existsById(1L)).thenReturn(true);
//        doNothing().when(categoryRepository).deleteById(1L); // 11. doNothing().when(): Para métodos void.
//
//        // When
//        categoryService.deleteCategory(1L);
//
//        // Then
//        verify(categoryRepository, times(1)).existsById(1L);
//        verify(categoryRepository, times(1)).deleteById(1L);
//    }
//
//    @Test
//    @DisplayName("Should throw ResourceNotFoundException when deleting non-existing category")
//    void shouldThrowResourceNotFoundExceptionOnDeleteNonExisting() {
//        // Given
//        when(categoryRepository.existsById(99L)).thenReturn(false);
//
//        // When & Then
//        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
//            categoryService.deleteCategory(99L);
//        });
//        assertEquals("Categoría no encontrada con ID: 99", exception.getMessage());
//        verify(categoryRepository, times(1)).existsById(99L);
//        verify(categoryRepository, never()).deleteById(anyLong());
//    }
//
//    @Test
//    @DisplayName("patchCategory should update category name and description partially")
//    void patchCategory_shouldUpdateCategoryPartially() {
//        Long categoryId = 1L;
//        Category existingCategory = new Category(categoryId, "Old Name", "Old Description");
//        CategoryPartialUpdateDTO patchDTO = new CategoryPartialUpdateDTO("New Name", "New Description");
//        Category updatedCategory = new Category(categoryId, "New Name", "New Description");
//
//        // Mockear el comportamiento del repositorio
//        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
//        when(categoryRepository.existsByName("New Name")).thenReturn(false); // No existe con el nuevo nombre
//        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
//
//        CategoryDTO resultDTO = categoryService.patchCategory(categoryId, patchDTO);
//
//        // Verificar aserciones
//        assertThat(resultDTO).isNotNull();
//        assertThat(resultDTO.id()).isEqualTo(categoryId);
//        assertThat(resultDTO.name()).isEqualTo("New Name");
//        assertThat(resultDTO.description()).isEqualTo("New Description");
//
//        // Verificar que los métodos del repositorio fueron llamados correctamente
//        verify(categoryRepository, times(1)).findById(categoryId);
//        verify(categoryRepository, times(1)).existsByName("New Name");
//        verify(categoryRepository, times(1)).save(existingCategory);
//    }
//
//    @Test
//    @DisplayName("patchCategory should update only category name")
//    void patchCategory_shouldUpdateOnlyCategoryName() {
//        Long categoryId = 1L;
//        Category existingCategory = new Category(categoryId, "Old Name", "Original Description");
//        CategoryPartialUpdateDTO patchDTO = new CategoryPartialUpdateDTO("New Name Only", null); // Solo el nombre
//        Category updatedCategory = new Category(categoryId, "New Name Only", "Original Description");
//
//        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
//        when(categoryRepository.existsByName("New Name Only")).thenReturn(false);
//        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
//
//        CategoryDTO resultDTO = categoryService.patchCategory(categoryId, patchDTO);
//
//        assertThat(resultDTO).isNotNull();
//        assertThat(resultDTO.id()).isEqualTo(categoryId);
//        assertThat(resultDTO.name()).isEqualTo("New Name Only");
//        assertThat(resultDTO.description()).isEqualTo("Original Description"); // Descripción original debe permanecer
//
//        verify(categoryRepository, times(1)).findById(categoryId);
//        verify(categoryRepository, times(1)).existsByName("New Name Only");
//        verify(categoryRepository, times(1)).save(existingCategory);
//    }
//
//    @Test
//    @DisplayName("patchCategory should update only category description")
//    void patchCategory_shouldUpdateOnlyCategoryDescription() {
//        Long categoryId = 1L;
//        Category existingCategory = new Category(categoryId, "Original Name", "Old Description");
//        CategoryPartialUpdateDTO patchDTO = new CategoryPartialUpdateDTO(null, "New Description Only"); // Solo la descripción
//        Category updatedCategory = new Category(categoryId, "Original Name", "New Description Only");
//
//        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
//        // No se llama existsByName si el nombre no cambia
//        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
//
//        CategoryDTO resultDTO = categoryService.patchCategory(categoryId, patchDTO);
//
//        assertThat(resultDTO).isNotNull();
//        assertThat(resultDTO.id()).isEqualTo(categoryId);
//        assertThat(resultDTO.name()).isEqualTo("Original Name"); // Nombre original debe permanecer
//        assertThat(resultDTO.description()).isEqualTo("New Description Only");
//
//        verify(categoryRepository, times(1)).findById(categoryId);
//        verify(categoryRepository, never()).existsByName(anyString()); // Verifica que no se llamó
//        verify(categoryRepository, times(1)).save(existingCategory);
//    }
//
//    @Test
//    @DisplayName("patchCategory should throw ResourceNotFoundException if category does not exist")
//    void patchCategory_shouldThrowResourceNotFoundException() {
//        Long nonExistentId = 99L;
//        CategoryPartialUpdateDTO patchDTO = new CategoryPartialUpdateDTO("Any Name", "Any Description");
//
//        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class, () ->
//                categoryService.patchCategory(nonExistentId, patchDTO));
//
//        verify(categoryRepository, times(1)).findById(nonExistentId);
//        verify(categoryRepository, never()).save(any(Category.class)); // No debe guardar
//    }
//
//    @Test
//    @DisplayName("patchCategory should throw DuplicateResourceException if new name already exists")
//    void patchCategory_shouldThrowDuplicateResourceExceptionWhenNewNameExists() {
//        Long categoryId = 1L;
//        Category existingCategory = new Category(categoryId, "Original Name", "Description");
//        CategoryPartialUpdateDTO patchDTO = new CategoryPartialUpdateDTO("Existing Name", null);
//
//        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
//        when(categoryRepository.existsByName("Existing Name")).thenReturn(true); // Ya existe este nombre
//
//        assertThrows(DuplicateResourceException.class, () ->
//                categoryService.patchCategory(categoryId, patchDTO));
//
//        verify(categoryRepository, times(1)).findById(categoryId);
//        verify(categoryRepository, times(1)).existsByName("Existing Name");
//        verify(categoryRepository, never()).save(any(Category.class));
//    }
//
//    @Test
//    @DisplayName("patchCategory should not throw DuplicateResourceException if name is updated to its current name")
//    void patchCategory_shouldNotThrowDuplicateResourceExceptionIfNameToCurrentName() {
//        Long categoryId = 1L;
//        Category existingCategory = new Category(categoryId, "Current Name", "Description");
//        CategoryPartialUpdateDTO patchDTO = new CategoryPartialUpdateDTO("Current Name", "New Desc");
//
//        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
//        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory); // Simula el guardado
//
//        CategoryDTO resultDTO = categoryService.patchCategory(categoryId, patchDTO);
//
//        assertThat(resultDTO).isNotNull();
//        assertThat(resultDTO.name()).isEqualTo("Current Name");
//        assertThat(resultDTO.description()).isEqualTo("New Desc");
//
//        verify(categoryRepository, times(1)).findById(categoryId);
//        verify(categoryRepository, never()).existsByName(anyString()); // No se llama si el nombre no cambia o es el mismo
//        verify(categoryRepository, times(1)).save(existingCategory);
//    }
//
//    @Test
//    @DisplayName("patchCategory should do nothing if patchDTO is empty/null fields")
//    void patchCategory_shouldDoNothingIfPatchDTOIsEmpty() {
//        Long categoryId = 1L;
//        Category existingCategory = new Category(categoryId, "Electronics", "Devices and gadgets");
//        CategoryPartialUpdateDTO patchDTO = new CategoryPartialUpdateDTO(null, null); // DTO vacío
//
//        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
//        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);
//
//        CategoryDTO resultDTO = categoryService.patchCategory(categoryId, patchDTO);
//
//        assertThat(resultDTO).isNotNull();
//        assertThat(resultDTO.name()).isEqualTo("Electronics");
//        assertThat(resultDTO.description()).isEqualTo("Devices and gadgets");
//
//        verify(categoryRepository, times(1)).findById(categoryId);
//        verify(categoryRepository, never()).existsByName(anyString());
//        verify(categoryRepository, times(1)).save(existingCategory); // Se llama save porque la entidad se actualiza (aunque no cambie)
//    }
//
//    // Método de mapeo auxiliar si no está ya en tu test
//    private CategoryDTO mapToDTO(Category category) {
//        return new CategoryDTO(category.getId(), category.getName(), category.getDescription());
//    }
//}

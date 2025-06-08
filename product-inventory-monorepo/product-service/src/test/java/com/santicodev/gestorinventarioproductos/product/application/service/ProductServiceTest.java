package com.santicodev.gestorinventarioproductos.product.application.service;

import com.santicodev.gestorinventarioproductos.category.domain.Category;
import com.santicodev.gestorinventarioproductos.category.infraestructure.repository.CategoryRepository;
import com.santicodev.gestorinventarioproductos.product.domain.Product;
import com.santicodev.gestorinventarioproductos.common.infraestructure.dto.ProductDTO;
import com.santicodev.gestorinventarioproductos.product.infraestructure.repository.ProductRepository;
import com.santicodev.gestorinventarioproductos.common.domain.exception.DuplicateResourceException;
import com.santicodev.gestorinventarioproductos.common.domain.exception.ResourceNotFoundException;
import com.santicodev.gestorinventarioproductos.common.infraestructure.dto.ProductPartialUpdateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository; // También necesitamos mockear CategoryRepository

    @InjectMocks
    private ProductService productService;

    private Category category1;
    private Category category2;
    private Product product1;
    private ProductDTO productDTO1;

    @BeforeEach
    void setUp() {
        category1 = new Category(1L, "Electronics", "Devices");
        category2 = new Category(2L, "Home", "Household items");
        product1 = new Product(1L, "Laptop", "Powerful laptop", new BigDecimal("1200.00"), 10, category1);
        productDTO1 = new ProductDTO(1L, "Laptop", "Powerful laptop", new BigDecimal("1200.00"), 10, 1L);
    }

    @Test
    @DisplayName("Should create a new product successfully")
    void shouldCreateProductSuccessfully() {
        // Given
        when(productRepository.existsByName("Laptop")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        // When
        ProductDTO result = productService.createProduct(productDTO1);

        // Then
        assertNotNull(result);
        assertEquals(productDTO1.name(), result.name());
        assertEquals(productDTO1.categoryId(), result.categoryId());
        verify(productRepository, times(1)).existsByName("Laptop");
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating product with existing name")
    void shouldThrowDuplicateResourceExceptionOnCreateExistingName() {
        // Given
        when(productRepository.existsByName("Laptop")).thenReturn(true);

        // When & Then
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            productService.createProduct(productDTO1);
        });
        assertEquals("El producto con el nombre 'Laptop' ya existe.", exception.getMessage());
        verify(productRepository, times(1)).existsByName("Laptop");
        verify(categoryRepository, never()).findById(anyLong()); // No debe intentar buscar categoría
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating product with non-existing category")
    void shouldThrowResourceNotFoundExceptionOnCreateNonExistingCategory() {
        // Given
        when(productRepository.existsByName("Laptop")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTO1);
        });
        assertEquals("Categoría no encontrada con ID: 1", exception.getMessage());
        verify(productRepository, times(1)).existsByName("Laptop");
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should return all products successfully")
    void shouldReturnAllProductsSuccessfully() {
        // Given
        Product product2 = new Product(2L, "Mouse", "Gaming mouse", new BigDecimal("50.00"), 50, category1);
        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<ProductDTO> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).name());
        assertEquals("Mouse", result.get(1).name());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return product by ID successfully")
    void shouldReturnProductByIdSuccessfully() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        // When
        ProductDTO result = productService.getProductById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Laptop", result.name());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when getting product by non-existing ID")
    void shouldThrowResourceNotFoundExceptionOnGetNonExistingId() {
        // Given
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(99L);
        });
        assertEquals("Producto no encontrado con ID: 99", exception.getMessage());
        verify(productRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Should update an existing product successfully")
    void shouldUpdateProductSuccessfully() {
        // Given
        ProductDTO updatedProductDTO = new ProductDTO(1L, "Laptop Pro", "New desc", new BigDecimal("1500.00"), 8, 1L);
        Product updatedProductEntity = new Product(1L, "Laptop Pro", "New desc", new BigDecimal("1500.00"), 8, category1);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.existsByName("Laptop Pro")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProductEntity);

        // When
        ProductDTO result = productService.updateProduct(1L, updatedProductDTO);

        // Then
        assertNotNull(result);
        assertEquals("Laptop Pro", result.name());
        assertEquals(new BigDecimal("1500.00"), result.price());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).existsByName("Laptop Pro");
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete an existing product successfully")
    void shouldDeleteProductSuccessfully() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        // When
        productService.deleteProduct(1L);

        // Then
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existing product")
    void shouldThrowResourceNotFoundExceptionOnDeleteNonExisting() {
        // Given
        when(productRepository.existsById(99L)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct(99L);
        });
        assertEquals("Producto no encontrado con ID: 99", exception.getMessage());
        verify(productRepository, times(1)).existsById(99L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should return products by category ID successfully")
    void shouldReturnProductsByCategoryIdSuccessfully() {
        // Given
        Product product2 = new Product(2L, "Headphones", "Noise cancelling", new BigDecimal("200.00"), 30, category1);
        List<Product> products = Arrays.asList(product1, product2);

        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findByCategoryId(1L)).thenReturn(products);

        // When
        List<ProductDTO> result = productService.getProductsByCategoryId(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).name());
        assertEquals("Headphones", result.get(1).name());
        verify(categoryRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).findByCategoryId(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when getting products by non-existing category ID")
    void shouldThrowResourceNotFoundExceptionOnGetProductsByNonExistingCategory() {
        // Given
        when(categoryRepository.existsById(99L)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductsByCategoryId(99L);
        });
        assertEquals("Categoría no encontrada con ID: 99", exception.getMessage());
        verify(categoryRepository, times(1)).existsById(99L);
        verify(productRepository, never()).findByCategoryId(anyLong());
    }

    @Test
    @DisplayName("patchProduct should update product name, description, price, stock, and category partially")
    void patchProduct_shouldUpdateProductPartially() {
        Long productId = 101L;
        Category newCategory = new Category(3L, "Office", "Office supplies");
        Product existingProduct = new Product(productId, "Old Product Name", "Old Desc", new BigDecimal("100.00"), 5, category1);
        ProductPartialUpdateDTO patchDTO = new ProductPartialUpdateDTO("New Name", "New Desc", new BigDecimal("150.00"), 10, newCategory.getId());
        Product updatedProduct = new Product(productId, "New Name", "New Desc", new BigDecimal("150.00"), 10, newCategory);

        // Mockear el comportamiento del repositorio
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByName("New Name")).thenReturn(false);
        when(categoryRepository.findById(newCategory.getId())).thenReturn(Optional.of(newCategory));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductDTO resultDTO = productService.patchProduct(productId, patchDTO);

        // Verificar aserciones
        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO.id()).isEqualTo(productId);
        assertThat(resultDTO.name()).isEqualTo("New Name");
        assertThat(resultDTO.description()).isEqualTo("New Desc");
        assertThat(resultDTO.price()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(resultDTO.stock()).isEqualTo(10);
        assertThat(resultDTO.categoryId()).isEqualTo(newCategory.getId());

        // Verificar que los métodos del repositorio fueron llamados correctamente
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).existsByName("New Name");
        verify(categoryRepository, times(1)).findById(newCategory.getId());
        verify(productRepository, times(1)).save(existingProduct); // save recibe la entidad modificada
    }

    @Test
    @DisplayName("patchProduct should update only product stock")
    void patchProduct_shouldUpdateOnlyProductStock() {
        Long productId = 101L;
        Product existingProduct = new Product(productId, "Laptop", "Powerful laptop", new BigDecimal("1200.00"), 10, category1);
        ProductPartialUpdateDTO patchDTO = new ProductPartialUpdateDTO(null, null, null, 15, null); // Solo stock
        Product updatedProduct = new Product(productId, "Laptop", "Powerful laptop", new BigDecimal("1200.00"), 15, category1);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductDTO resultDTO = productService.patchProduct(productId, patchDTO);

        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO.id()).isEqualTo(productId);
        assertThat(resultDTO.stock()).isEqualTo(15);
        assertThat(resultDTO.name()).isEqualTo("Laptop"); // Otros campos deben permanecer
        assertThat(resultDTO.price()).isEqualByComparingTo(new BigDecimal("1200.00"));

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(existingProduct);
        verify(categoryRepository, never()).findById(anyLong()); // No debe buscar categoría si no se pasa
        verify(productRepository, never()).existsByName(anyString()); // No debe verificar nombre si no cambia
    }

    @Test
    @DisplayName("patchProduct should throw ResourceNotFoundException if product does not exist")
    void patchProduct_shouldThrowResourceNotFoundExceptionWhenProductNotFound() {
        Long nonExistentId = 999L;
        ProductPartialUpdateDTO patchDTO = new ProductPartialUpdateDTO("Any Name", null, null, null, null);

        when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                productService.patchProduct(nonExistentId, patchDTO));

        verify(productRepository, times(1)).findById(nonExistentId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("patchProduct should throw DuplicateResourceException if new name already exists")
    void patchProduct_shouldThrowDuplicateResourceExceptionWhenNewNameExists() {
        Long productId = 101L;
        Product existingProduct = new Product(productId, "Laptop", "Description", new BigDecimal("100.00"), 5, category1);
        ProductPartialUpdateDTO patchDTO = new ProductPartialUpdateDTO("Existing Product Name", null, null, null, null);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByName("Existing Product Name")).thenReturn(true); // Nombre ya existe

        assertThrows(DuplicateResourceException.class, () ->
                productService.patchProduct(productId, patchDTO));

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).existsByName("Existing Product Name");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("patchProduct should throw ResourceNotFoundException if new category ID does not exist")
    void patchProduct_shouldThrowResourceNotFoundExceptionWhenCategoryNotFound() {
        Long productId = 101L;
        Long nonExistentCategoryId = 99L;
        Product existingProduct = new Product(productId, "Laptop", "Description", new BigDecimal("100.00"), 5, category1);
        ProductPartialUpdateDTO patchDTO = new ProductPartialUpdateDTO(null, null, null, null, nonExistentCategoryId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(nonExistentCategoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                productService.patchProduct(productId, patchDTO));

        verify(productRepository, times(1)).findById(productId);
        verify(categoryRepository, times(1)).findById(nonExistentCategoryId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("patchProduct should not throw DuplicateResourceException if name is updated to its current name")
    void patchProduct_shouldNotThrowDuplicateResourceExceptionIfNameToCurrentName() {
        Long productId = 101L;
        Product existingProduct = new Product(productId, "Laptop", "Description", new BigDecimal("100.00"), 5, category1);
        ProductPartialUpdateDTO patchDTO = new ProductPartialUpdateDTO("Laptop", "New Desc", null, null, null);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        ProductDTO resultDTO = productService.patchProduct(productId, patchDTO);

        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO.name()).isEqualTo("Laptop");
        assertThat(resultDTO.description()).isEqualTo("New Desc");

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).existsByName(anyString()); // No se llama existsByName
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    @DisplayName("patchProduct should do nothing if patchDTO is empty/null fields")
    void patchProduct_shouldDoNothingIfPatchDTOIsEmpty() {
        Long productId = 101L;
        Product existingProduct = new Product(productId, "Laptop", "Powerful laptop", new BigDecimal("1200.00"), 10, category1);
        ProductPartialUpdateDTO patchDTO = new ProductPartialUpdateDTO(null, null, null, null, null); // DTO vacío

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct); // Simula el guardado

        ProductDTO resultDTO = productService.patchProduct(productId, patchDTO);

        assertThat(resultDTO).isNotNull();
        assertThat(resultDTO.name()).isEqualTo("Laptop");
        assertThat(resultDTO.stock()).isEqualTo(10);
        // Asegúrate de que otros campos permanezcan sin cambios
        assertThat(resultDTO.description()).isEqualTo("Powerful laptop");
        assertThat(resultDTO.price()).isEqualByComparingTo(new BigDecimal("1200.00"));

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).existsByName(anyString());
        verify(categoryRepository, never()).findById(anyLong());
        verify(productRepository, times(1)).save(existingProduct);
    }

    // Método de mapeo auxiliar si no está ya en tu test
    private ProductDTO mapToDTO(Product product) {
        return new ProductDTO(product.getId(), product.getName(), product.getDescription(),
                product.getPrice(), product.getStock(),
                product.getCategory() != null ? product.getCategory().getId() : null);
    }
}

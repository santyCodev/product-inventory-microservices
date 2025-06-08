package com.santicodev.gestorinventarioproductos.integration.product;

import com.santicodev.gestorinventarioproductos.category.domain.Category;
import com.santicodev.gestorinventarioproductos.category.infraestructure.repository.CategoryRepository;
import com.santicodev.gestorinventarioproductos.integration.config.BaseIntegrationTest;
import com.santicodev.gestorinventarioproductos.product.application.service.ProductService;
import com.santicodev.gestorinventarioproductos.product.domain.Product;
import com.santicodev.gestorinventarioproductos.product.infraestructure.repository.ProductRepository;
import com.santicodev.gestorinventarioproductos.common.domain.exception.ResourceNotFoundException;
import com.santicodev.gestorinventarioproductos.common.infraestructure.dto.ProductDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para la capa de servicio de Productos.
 * Extiende BaseIntegrationTest para obtener la configuración de Testcontainers y Spring Boot.
 * Se enfoca en la lógica de negocio, la interacción con la base de datos (y categorías), y la caché.
 */
@DisplayName("ProductService Integration Tests")
class ProductServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository; // Para gestionar las categorías asociadas a productos

    /**
     * Limpieza de la base de datos antes de cada prueba.
     * Es vital limpiar tanto productos como categorías debido a su relación.
     */
    @Override
    @BeforeEach
    public void setupBase() {
        super.setupBase(); // Llama al metodo de limpieza de la clase base (para Redis)
        productRepository.deleteAll(); // Limpia la tabla de productos
        // **CRÍTICO:** Limpia la tabla de categorías también para asegurar un estado limpio
        categoryRepository.deleteAll();
        // ya que los productos tienen una relación con las categorías.
    }

    @Test
    @DisplayName("Debería guardar un nuevo producto correctamente con asociación de categoría")
    void shouldSaveNewProductWithCategoryAssociation() {
        // ARRANGE: Crear y guardar una categoría primero, ya que es una dependencia de Product.
        Category category = new Category(null,"Electrónica", "Dispositivos electrónicos");
        categoryRepository.save(category);

        ProductDTO newProduct = new ProductDTO(null,
                "Smartphone Avanzado",
                "Último modelo con características avanzadas",
                BigDecimal.valueOf(899.99), // Usa BigDecimal para precisión financiera
                100,
                category.getId() // Asocia el producto a la categoría

        );
        // ACT: Guardar el producto a través del servicio.
        ProductDTO savedProduct = productService.createProduct(newProduct);

        // ASSERT: Verificar que el producto se guardó correctamente.
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.id()).isNotNull();
        assertThat(savedProduct.name()).isEqualTo("Smartphone Avanzado");
        assertThat(savedProduct.price()).isEqualByComparingTo(BigDecimal.valueOf(899.99)); // Comparación de BigDecimal
        assertThat(savedProduct.stock()).isEqualTo(100);

        // Verificar la asociación con la categoría.
        assertThat(savedProduct.categoryId()).isNotNull();
        assertThat(savedProduct.categoryId()).isEqualTo(category.getId());

        // Verificación directa en la base de datos.
        Optional<Product> foundInDb = productRepository.findById(savedProduct.id());
        Optional<Category> foundCatInDb = categoryRepository.findById(savedProduct.categoryId());
        assertTrue(foundInDb.isPresent());
        assertTrue(foundCatInDb.isPresent());
        assertThat(foundInDb.get().getName()).isEqualTo("Smartphone Avanzado");
        assertThat(foundInDb.get().getCategory().getId()).isEqualTo(foundCatInDb.get().getId());
    }

    @Test
    @DisplayName("Debería obtener un producto por ID con su categoría asociada")
    void shouldGetProductByIdWithCategory() {
        // ARRANGE: Guardar una categoría y un producto asociado.
        Category category = new Category(null,"Libros", "Medios impresos");
        categoryRepository.save(category);

        Product existingProduct =
                new Product(null,"Tablet Pro", "Tablet de alto rendimiento", BigDecimal.valueOf(499.00), 50, category);

        productRepository.save(existingProduct);

        // ACT: Obtener el producto por ID.
        ProductDTO foundProduct = productService.getProductById(existingProduct.getId());

        // ASSERT: Verificar el producto y su categoría.
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.name()).isEqualTo("Tablet Pro");
        assertThat(foundProduct.categoryId()).isNotNull();
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException si el producto no se encuentra por ID")
    void shouldThrowNotFoundExceptionWhenProductNotFound() {
        // ACT & ASSERT: Intentar obtener un producto con un ID que no existe.
        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(999L),
                "Se esperaba ResourceNotFoundException para un producto no encontrado");
    }

    @Test
    @DisplayName("Debería obtener todos los productos con sus categorías asociadas")
    void shouldGetAllProductsWithCategories() {
        // ARRANGE: Guardar varias categorías y productos asociados.
        Category cat1 = categoryRepository.save(new Category(null,"Ropa", "Todo tipo de prendas"));
        Category cat2 = categoryRepository.save(new Category(null,"Hogar", "Artículos para el hogar"));

        Product prod1 = new Product(null,"Camiseta Algodón", "100% algodón", BigDecimal.valueOf(15.0), 100, cat1);
        productRepository.save(prod1);

        Product prod2 = new Product(null,"Lámpara de Escritorio", "Lámpara LED", BigDecimal.valueOf(30.0), 50, cat2);
        productRepository.save(prod2);

        // ACT: Obtener todos los productos.
        List<ProductDTO> products = productService.getAllProducts();

        // ASSERT: Verificar la cantidad y los datos de los productos y sus categorías.
        assertThat(products).isNotNull().hasSize(2);
        assertThat(products).extracting(ProductDTO::name).containsExactlyInAnyOrder("Camiseta Algodón", "Lámpara de Escritorio");
        assertThat(products).filteredOn(p -> p.name().equals("Camiseta Algodón"))
                .first()
                .extracting(ProductDTO::categoryId)
                .isEqualTo(cat1.getId());
        assertThat(products).filteredOn(p -> p.name().equals("Lámpara de Escritorio"))
                .first()
                .extracting(ProductDTO::categoryId)
                .isEqualTo(cat2.getId());
    }

    @Test
    @DisplayName("Debería actualizar un producto existente incluyendo su categoría")
    void shouldUpdateProductIncludingCategory() {
        // ARRANGE: Guardar una categoría original y un producto, y una nueva categoría.
        Category originalCategory = categoryRepository.save(new Category(null,"Categoría Original", "Descripción Original"));
        Category newCategory = categoryRepository.save(new Category(null,"Nueva Categoría", "Nueva Descripción"));

        Product existingProduct =
                new Product(null, "Nombre Antiguo", "Descripción Antigua", BigDecimal.valueOf(100.0), 10, originalCategory);
        productRepository.save(existingProduct);

        // Crear un objeto Product con los nuevos detalles, incluyendo la nueva categoría.
        ProductDTO updatedDetails = new ProductDTO(
                null,
                "Nuevo Nombre de Producto",
                "Nueva Descripción de Producto",
                BigDecimal.valueOf(150.0),
                15,
                newCategory.getId()
                );

        // ACT: Actualizar el producto a través del servicio.
        ProductDTO updatedProduct = productService.updateProduct(existingProduct.getId(), updatedDetails);

        // ASSERT: Verificar que el producto devuelto refleja la actualización.
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.id()).isEqualTo(existingProduct.getId());
        assertThat(updatedProduct.name()).isEqualTo("Nuevo Nombre de Producto");
        assertThat(updatedProduct.price()).isEqualByComparingTo(BigDecimal.valueOf(150.0));
        assertThat(updatedProduct.stock()).isEqualTo(15);
        assertThat(updatedProduct.categoryId()).isNotNull();
        assertThat(updatedProduct.categoryId()).isEqualTo(newCategory.getId()); // Verificar que la categoría fue cambiada

        // Verificación directa en la base de datos.
        Optional<Product> foundInDb = productRepository.findById(updatedProduct.id());
        Optional<Category> foundCatInDb = categoryRepository.findById(updatedProduct.categoryId());
        assertTrue(foundInDb.isPresent());
        assertTrue(foundCatInDb.isPresent());
        assertThat(foundInDb.get().getName()).isEqualTo("Nuevo Nombre de Producto");
        assertThat(foundInDb.get().getCategory().getId()).isEqualTo(foundCatInDb.get().getId());
    }

    @Test
    @DisplayName("Debería eliminar un producto por ID")
    void shouldDeleteProduct() {
        // ARRANGE: Guardar un producto y su categoría.
        Category category = categoryRepository.save(
                new Category(null,"Temporal", "Para prueba de eliminación"));
        Product productToDelete =
                new Product(null,"Producto a Eliminar", "Descripción", BigDecimal.valueOf(50.0), 5, category);
        productRepository.save(productToDelete);

        // ACT: Eliminar el producto.
        productService.deleteProduct(productToDelete.getId());

        // ASSERT: Verificar que el producto ya no existe en la base de datos.
        assertFalse(productRepository.existsById(productToDelete.getId()));
    }

    @Test
    @DisplayName("Debería lanzar ResourceNotFoundException al intentar actualizar un producto inexistente")
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistentProduct() {
        // ARRANGE: Datos para la actualización, pero para un ID de producto no existente.
        Category category = categoryRepository.save(new Category(null,"Dummy", "Para prueba de error"));
        // Necesario asignar una categoría aunque el producto no exista.
        ProductDTO updateData = new ProductDTO(
                null,"No Existe", "Desc", BigDecimal.valueOf(10.0), 1,category.getId());

        // ACT & ASSERT: Se espera una ResourceNotFoundException.
        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(999L, updateData),
                "Se esperaba ResourceNotFoundException al intentar actualizar un producto que no existe.");
    }

    @Test
    @DisplayName("Debería actualizar el stock de un producto")
    void shouldUpdateProductStock() {
        // ARRANGE: Crear un producto con un stock inicial.
        Category category = categoryRepository.save(new Category(null,"Almacén", "Productos en inventario"));
        Product existingProduct =
                new Product(null,"Artículo de Stock", "Descripción", BigDecimal.valueOf(10.0), 20, category);
        productRepository.save(existingProduct);

        // Nuevo valor de stock, lo añadimos al DTO
        int newStock = 15;
        ProductDTO existingProductDTO =
                new ProductDTO(null,"Artículo de Stock", "Descripción", BigDecimal.valueOf(10.0), newStock, category.getId());


        // ACT: Actualizar el stock a través del servicio.
        ProductDTO updatedProduct = productService.updateProduct(existingProduct.getId(), existingProductDTO);

        // ASSERT: Verificar que el stock se actualizó.
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.id()).isEqualTo(existingProduct.getId());
        assertThat(updatedProduct.stock()).isEqualTo(newStock);

        // Verificar en la DB
        Optional<Product> foundInDb = productRepository.findById(existingProduct.getId());
        assertTrue(foundInDb.isPresent());
        assertThat(foundInDb.get().getStock()).isEqualTo(newStock);
    }

    @Test
    @DisplayName("Debería interactuar con la caché de Redis al obtener un producto (asumiendo @Cacheable)")
    void shouldInteractWithRedisCacheWhenFetchingProduct() {
        // ARRANGE: Crear una categoría y un producto para probar la caché.
        Category category = categoryRepository.save(new Category(null, "Caché", "Categoría para pruebas de caché"));
        Product product = new Product(null, "Producto Caché", "Descripción para caché", BigDecimal.valueOf(25.0), 5, category);
        Product savedProduct = productRepository.save(product);

        // Limpiar la clave de caché explícitamente antes de la primera llamada si la clave es predecible
        // Esto es opcional y depende de cómo esté configurada tu caché.
        if (redisTemplate != null) {
            redisTemplate.delete("products::" + savedProduct.getId());
            assertThat(redisTemplate.hasKey("products::" + savedProduct.getId())).isFalse();
        }

        // ACT: Primera llamada, debería ir a la DB y cachear el resultado (asumiendo @Cacheable("products")).
        productService.getProductById(savedProduct.getId());

        // ASSERT (verificación de caché): Si tienes acceso a RedisTemplate, puedes verificar si la clave existe.
        // La clave de caché típicamente sería "nombreDeCaché::ID".
        if (redisTemplate != null) {
            // Esperamos un breve momento para que la caché se propague, aunque en un entorno de test local es casi instantáneo
            // try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            assertThat(redisTemplate.hasKey("products::" + savedProduct.getId())).isTrue();
        }

        // ACT: Segunda llamada, debería obtenerse de la caché sin ir a la DB.
        // Aquí no podemos verificar directamente que no fue a la DB sin Mockito,
        // pero la presencia en caché es una buena indicación.

        // Ahora, probemos la invalidación de caché si actualizamos.
        // Si tu `updateProductStock` o `updateProduct` tiene `@CacheEvict(value="products", key="#id")`

        int newStock = 3;
        ProductDTO existingProductDTO =
                new ProductDTO(null,"Artículo de Stock", "Descripción", BigDecimal.valueOf(10.0), newStock, category.getId());
        productService.updateProduct(savedProduct.getId(), existingProductDTO);

        // ASSERT (verificación de invalidación): La clave de caché debería haber sido eliminada.
        if (redisTemplate != null) {
            assertThat(redisTemplate.hasKey("products::" + savedProduct.getId())).isFalse();
        }

        // ACT: Tercera llamada, debería ir a la DB de nuevo y re-cachear.
        ProductDTO updatedAndFetched = productService.getProductById(savedProduct.getId());
        assertThat(updatedAndFetched.stock()).isEqualTo(newStock); // Verifica que se obtuvo el stock actualizado

        if (redisTemplate != null) {
            assertThat(redisTemplate.hasKey("products::" + savedProduct.getId())).isTrue();
        }
    }
}


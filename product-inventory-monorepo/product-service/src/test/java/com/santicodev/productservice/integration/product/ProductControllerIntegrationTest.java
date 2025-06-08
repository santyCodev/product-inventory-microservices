//package com.santicodev.productservice.integration.product;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.santicodev.commondomainlib.infraestructure.dto.ProductDTO;
//import com.santicodev.commondomainlib.infraestructure.dto.ProductPartialUpdateDTO;
//import com.santicodev.productservice.category.domain.Category;
//import com.santicodev.productservice.category.infraestructure.repository.CategoryRepository;
//import com.santicodev.productservice.domain.Product;
//import com.santicodev.productservice.infraestructure.repository.ProductRepository;
//import com.santicodev.productservice.integration.config.BaseIntegrationTest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import java.math.BigDecimal;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.hamcrest.Matchers.hasSize;
//import static org.hamcrest.Matchers.is;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
///**
// * Pruebas de integración para la capa de controlador (API REST) de Productos.
// * Simula peticiones HTTP de extremo a extremo, verificando la validación de DTOs,
// * la seguridad y las respuestas HTTP. Incluye pruebas específicas para la validación.
// */
//@DisplayName("ProductController Integration Tests")
//class ProductControllerIntegrationTest extends BaseIntegrationTest {
//
//    @Autowired
//    private WebApplicationContext webApplicationContext;
//
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Autowired
//    private CategoryRepository categoryRepository; // Necesario para crear categorías y asociarlas a productos
//
//    /**
//     * Configuración inicial antes de cada prueba.
//     * Limpia la base de datos de productos y categorías, y configura MockMvc.
//     */
//    @Override
//    @BeforeEach
//    public void setupBase() {
//        super.setupBase(); // Limpia Redis
//        productRepository.deleteAll(); // Limpia la tabla de productos
//        categoryRepository.deleteAll(); // Limpia la tabla de categorías (importante para relaciones)
//        this.mockMvc = MockMvcBuilders
//                .webAppContextSetup(webApplicationContext)
//                .apply(springSecurity()) // Aplica la configuración de Spring Security para MockMvc
//                .build();
//    }
//
//    // --- Pruebas para GET /api/products ---
//    @Test
//    @DisplayName("GET /api/v1/products debería retornar todos los productos (usuario autenticado)")
//    @WithMockUser(username = "user", roles = {"USER"}) // Un usuario normal debería poder ver productos
//    void getAllProducts_authenticatedUser_shouldReturnProducts() throws Exception {
//        // ARRANGE: Preparar una categoría y algunos productos en la base de datos.
//        Category electronics = categoryRepository.save(new Category(null, "Electrónica", "Dispositivos electrónicos."));
//        productRepository.save(
//                new Product(null, "Laptop Pro", "Portátil potente", BigDecimal.valueOf(1200.0), 10, electronics));
//        productRepository.save(
//                new Product(null, "Ratón Inalámbrico", "Ratón ergonómico", BigDecimal.valueOf(25.0), 200, electronics));
//
//        // ACT & ASSERT
//        mockMvc.perform(get("/api/v1/products")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2))) // Espera un array de 2 elementos
//                .andExpect(jsonPath("$[0].name", is("Laptop Pro")))
//                .andExpect(jsonPath("$[1].name", is("Ratón Inalámbrico"))); // El orden puede variar
//    }
//
//    @Test
//    @DisplayName("GET /api/v1/products debería retornar 401 Unauthorized para acceso no autenticado")
//    void getAllProducts_unauthenticatedUser_shouldReturnUnauthorized() throws Exception {
//        // ACT & ASSERT
//        mockMvc.perform(get("/api/v1/products")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isForbidden());
//    }
//
//    // --- Pruebas para GET /api/products/{id} ---
//    @Test
//    @DisplayName("GET /api/v1/products/{id} debería retornar producto por ID con su categoría")
//    @WithMockUser(username = "user", roles = {"USER"})
//    void getProductById_shouldReturnProduct() throws Exception {
//        // ARRANGE
//        Category gaming = categoryRepository.save(new Category(null, "Gaming", "Accesorios de juegos."));
//        Product existingProduct = productRepository.save(new Product(null, "Teclado Mecánico", "Teclado RGB", BigDecimal.valueOf(150.0), 50, gaming));
//
//        // ACT & ASSERT
//        mockMvc.perform(get("/api/v1/products/{id}", existingProduct.getId())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name", is("Teclado Mecánico")))
//                .andExpect(jsonPath("$.price", is(150.0)))
//                .andExpect(jsonPath("$.categoryId", is((Math.toIntExact(gaming.getId()))))); // Verifica la categoría anidada
//    }
//
//    @Test
//    @DisplayName("GET /api/v1/products/{id} debería retornar 404 Not Found para producto inexistente")
//    @WithMockUser(username = "user", roles = {"USER"})
//    void getProductById_nonExistent_shouldReturnNotFound() throws Exception {
//        // ACT & ASSERT
//        mockMvc.perform(get("/api/v1/products/{id}", 999L)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//    }
//
//    // --- Pruebas para POST /api/products ---
//    @Test
//    @DisplayName("POST /api/v1/products debería crear un nuevo producto (usuario ADMIN autenticado)")
//    @WithMockUser(username = "admin", roles = {"ADMIN"}) // Solo ADMIN puede crear productos
//    void createProduct_authenticatedAdmin_shouldCreateProduct() throws Exception {
//        // ARRANGE: Crear una categoría que servirá para el nuevo producto.
//        Category category = categoryRepository.save(new Category(null, "Deportes", "Artículos deportivos."));
//
//        // Crear un ProductDTO con datos válidos.
//        ProductDTO newProductDTO = new ProductDTO(null, "Balón de Fútbol", "Balón oficial",
//                BigDecimal.valueOf(30.50), 20, category.getId());
//
//        // ACT & ASSERT
//        mockMvc.perform(post("/api/v1/products")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(newProductDTO)))
//                .andExpect(status().isCreated()) // Espera 201 Created
//                .andExpect(jsonPath("$.id").exists())
//                .andExpect(jsonPath("$.name", is("Balón de Fútbol")))
//                .andExpect(jsonPath("$.categoryId", is((Math.toIntExact(category.getId()))))); // Verifica la categoría anidada
//
//        // Verificación directa en la base de datos
//        assertThat(productRepository.count()).isEqualTo(1);
//        assertThat(productRepository.findAll().getFirst().getName()).isEqualTo("Balón de Fútbol");
//    }
//
//    @Test
//    @DisplayName("POST /api/v1/products debería retornar 403 Forbidden para usuario USER autenticado")
//    @WithMockUser(username = "user", roles = {"USER"})
//    void createProduct_authenticatedUser_shouldReturnForbidden() throws Exception {
//        // ARRANGE
//        Category category = categoryRepository.save(new Category(null,"Juguetes", "Juguetes para niños."));
//        ProductDTO newProductDTO = new ProductDTO(null, "Muñeca", "Muñeca articulada",
//                BigDecimal.valueOf(10.0), 50, category.getId());
//
//        // ACT & ASSERT
//        mockMvc.perform(post("/api/v1/products")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(newProductDTO)))
//                .andExpect(status().isForbidden()); // Espera 403 Forbidden
//    }
//
//    @Test
//    @DisplayName("POST /api/v1/products debería retornar 404 ResourceNotFoundException si la categoría no existe")
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void createProduct_nonExistentCategory_shouldReturnBadRequest() throws Exception {
//        // ARRANGE: Un ProductDTO con un categoryId que no existe.
//        ProductDTO newProductDTO = new ProductDTO(null, "Producto sin Cat", "Descripción",
//                BigDecimal.valueOf(10.0), 10, 999L); // ID de categoría inexistente
//
//        // ACT & ASSERT
//        mockMvc.perform(post("/api/v1/products")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(newProductDTO)))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").exists()); // Verifica un mensaje de error
//    }
//
//    // --- Pruebas de Validación de DTO para POST /api/products ---
//    @Test
//    @DisplayName("POST /api/v1/products debería retornar 400 Bad Request para nombre de producto en blanco")
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void createProduct_invalidNameBlank_shouldReturnBadRequest() throws Exception {
//        // ARRANGE: ProductDTO con nombre en blanco (@NotBlank).
//        Category category = categoryRepository.save(new Category(null, "Cat", "Desc"));
//        ProductDTO invalidProductDTO = new ProductDTO(null, " ", "Desc", BigDecimal.ONE, 1, category.getId());
//
//        // ACT & ASSERT
//        mockMvc.perform(post("/api/v1/products")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidProductDTO)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").exists());
//    }
//
//    @Test
//    @DisplayName("POST /api/v1/products debería retornar 400 Bad Request para precio nulo")
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void createProduct_nullPrice_shouldReturnBadRequest() throws Exception {
//        // ARRANGE: ProductDTO con precio nulo (@NotNull).
//        Category category = categoryRepository.save(new Category(null, "Cat", "Desc"));
//        ProductDTO invalidProductDTO = new ProductDTO(null, "Producto", "Desc", null, 1, category.getId());
//
//        // ACT & ASSERT
//        mockMvc.perform(post("/api/v1/products")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidProductDTO)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").exists());
//    }
//
//    @Test
//    @DisplayName("POST /api/v1/products debería retornar 400 Bad Request para precio negativo")
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void createProduct_negativePrice_shouldReturnBadRequest() throws Exception {
//        // ARRANGE: ProductDTO con precio negativo (@Min(value=0)).
//        Category category = categoryRepository.save(new Category(null, "Cat", "Desc"));
//        ProductDTO invalidProductDTO = new ProductDTO(null, "Producto", "Desc", BigDecimal.valueOf(-10.0), 1, category.getId());
//
//        // ACT & ASSERT
//        mockMvc.perform(post("/api/v1/products")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidProductDTO)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").exists());
//    }
//
//    @Test
//    @DisplayName("POST /api/v1/products debería retornar 400 Bad Request para stock negativo")
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void createProduct_negativeStock_shouldReturnBadRequest() throws Exception {
//        // ARRANGE: ProductDTO con stock negativo (@PositiveOrZero).
//        Category category = categoryRepository.save(new Category(null, "Cat", "Desc"));
//        ProductDTO invalidProductDTO = new ProductDTO(null, "Producto", "Desc", BigDecimal.ONE, -5, category.getId());
//
//        // ACT & ASSERT
//        mockMvc.perform(post("/api/v1/products")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidProductDTO)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").exists());
//    }
//
//    // --- Pruebas para PUT /api/products/{id} ---
//    @Test
//    @DisplayName("PUT /api/v1/products/{id} debería actualizar un producto (usuario ADMIN autenticado)")
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void updateProduct_authenticatedAdmin_shouldUpdateProduct() throws Exception {
//        // ARRANGE
//        Category oldCategory = categoryRepository.save(new Category(null, "Antigua Cat", "Desc"));
//        Category newCategory = categoryRepository.save(new Category(null, "Nueva Cat", "Desc"));
//        Product existingProduct = productRepository.save(new Product(null, "Antiguo Nombre", "Antigua Desc", BigDecimal.valueOf(50.0), 5, oldCategory));
//
//        // Crear un ProductDTO con los datos actualizados, incluyendo la nueva categoría.
//        ProductDTO updatedProductDTO = new ProductDTO(existingProduct.getId(), "Nuevo Nombre", "Nueva Desc",
//                BigDecimal.valueOf(75.0), 10, newCategory.getId());
//
//        // ACT & ASSERT
//        mockMvc.perform(put("/api/v1/products/{id}", existingProduct.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updatedProductDTO)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name", is("Nuevo Nombre")))
//                .andExpect(jsonPath("$.price", is(75.0)))
//                .andExpect(jsonPath("$.categoryId", is((Math.toIntExact(newCategory.getId())))));
//
//        // Verificación directa en la base de datos
//        Product updatedInDb = productRepository.findById(existingProduct.getId()).orElseThrow();
//        Category updatedCatInDb = categoryRepository.findById(updatedInDb.getCategory().getId()).orElseThrow();
//        assertThat(updatedInDb.getName()).isEqualTo("Nuevo Nombre");
//        assertThat(updatedCatInDb.getId()).isEqualTo(newCategory.getId());
//    }
//
//    @Test
//    @DisplayName("PUT /api/v1/products/{id} debería retornar 404 Not Found si el producto a actualizar no existe")
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void updateProduct_nonExistent_shouldReturnNotFound() throws Exception {
//        // ARRANGE
//        Category category = categoryRepository.save(new Category(null, "Temp", "Temp Desc"));
//        ProductDTO nonExistentProductDTO = new ProductDTO(999L, "No Existe", "Desc", BigDecimal.ONE, 1, category.getId());
//
//        // ACT & ASSERT
//        mockMvc.perform(put("/api/v1/products/{id}", 999L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(nonExistentProductDTO)))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @DisplayName("PUT /api/v1/products/{id} debería retornar 404 Not Found si la categoría de actualización no existe")
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void updateProduct_nonExistentCategoryInUpdate_shouldReturnBadRequest() throws Exception {
//        // ARRANGE
//        Category oldCategory = categoryRepository.save(new Category(null, "Existing", "Desc"));
//        Product existingProduct = productRepository.save(new Product(null, "Old", "Desc", BigDecimal.TEN, 10, oldCategory));
//
//        // ProductDTO con un categoryId inexistente.
//        ProductDTO invalidUpdateDTO = new ProductDTO(existingProduct.getId(), "Valid Name", "Valid Desc",
//                BigDecimal.valueOf(100.0), 10, 999L);
//
//        // ACT & ASSERT
//        mockMvc.perform(put("/api/v1/products/{id}", existingProduct.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidUpdateDTO)))
//                .andExpect(status().isNotFound()) // O 404 si tu controlador lo mapea a ResourceNotFoundException
//                .andExpect(jsonPath("$.message").exists());
//    }
//
//    // --- Pruebas para PATCH /api/products/{id}/stock ---
//    @Test
//    @DisplayName("PATCH /api/v1/products/{id} debería actualizar el stock (usuario ADMIN autenticado)")
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void updateProductStock_authenticatedAdmin_shouldUpdateStock() throws Exception {
//        // ARRANGE
//        Category category = categoryRepository.save(new Category(null, "Almacén", "Productos para el almacén"));
//        Product existingProduct = productRepository.save(new Product(null, "Artículo", "Descripción", BigDecimal.valueOf(10.0), 20, category));
//
//        // Crear un DTO para la actualización parcial del stock.
//        ProductPartialUpdateDTO updateStockDTO = new ProductPartialUpdateDTO(null, null, null, 15, null); // Solo actualizamos stock
//
//        // ACT & ASSERT
//        mockMvc.perform(patch("/api/v1/products/{id}", existingProduct.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateStockDTO)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.stock", is(15))); // Verifica que el stock se actualizó
//
//        // Verificación directa en la base de datos
//        Product updatedInDb = productRepository.findById(existingProduct.getId()).orElseThrow();
//        assertThat(updatedInDb.getStock()).isEqualTo(15);
//    }
//
//    @Test
//    @DisplayName("PATCH /api/v1/products/{id} debería retornar 400 Bad Request para stock negativo")
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void updateProductStock_negativeStock_shouldReturnBadRequest() throws Exception {
//        // ARRANGE
//        Category category = categoryRepository.save(new Category(null, "Almacén", "Productos para el almacén"));
//        Product existingProduct = productRepository.save(new Product(null, "Artículo", "Descripción", BigDecimal.valueOf(10.0), 20, category));
//
//        ProductPartialUpdateDTO updateStockDTO = new ProductPartialUpdateDTO(null, null, null, -15, null); // Stock negativo
//
//        // ACT & ASSERT
//        mockMvc.perform(patch("/api/v1/products/{id}", existingProduct.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateStockDTO)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").exists());
//    }
//
//    @Test
//    @DisplayName("PATCH /api/v1/products/{id} debería retornar 404 Not Found si el producto no existe")
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void updateProductStock_nonExistent_shouldReturnNotFound() throws Exception {
//        // ARRANGE
//        ProductPartialUpdateDTO updateStockDTO = new ProductPartialUpdateDTO(null, null, null, null, null);
//
//        // ACT & ASSERT
//        mockMvc.perform(patch("/api/v1/products/{id}", 999L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateStockDTO)))
//                .andExpect(status().isNotFound());
//    }
//
//    // --- Pruebas para DELETE /api/products/{id} ---
//    @Test
//    @DisplayName("DELETE /api/v1/products/{id} debería eliminar un producto (usuario ADMIN autenticado)")
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void deleteProduct_authenticatedAdmin_shouldDeleteProduct() throws Exception {
//        // ARRANGE
//        Category category = categoryRepository.save(new Category(null, "Basura", "Productos para borrar"));
//        Product productToDelete = productRepository.save(new Product(null, "Producto para Borrar", "Desc", BigDecimal.valueOf(5.0), 1, category));
//
//        // ACT & ASSERT
//        mockMvc.perform(delete("/api/v1/products/{id}", productToDelete.getId()))
//                .andExpect(status().isNoContent()); // Espera 204 No Content
//
//        // Verificación directa en la base de datos
//        assertFalse(productRepository.existsById(productToDelete.getId()));
//    }
//
//    @Test
//    @DisplayName("DELETE /api/v1/products/{id} debería retornar 404 Not Found si el producto a eliminar no existe")
//    @WithMockUser(username = "admin", roles = {"ADMIN"})
//    void deleteProduct_nonExistent_shouldReturnNotFound() throws Exception {
//        // ACT & ASSERT
//        mockMvc.perform(delete("/api/v1/products/{id}", 999L))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @DisplayName("DELETE /api/v1/products/{id} debería retornar 403 Forbidden para usuario USER autenticado")
//    @WithMockUser(username = "user", roles = {"USER"})
//    void deleteProduct_authenticatedUser_shouldReturnForbidden() throws Exception {
//        // ARRANGE
//        Category category = categoryRepository.save(new Category(null, "Protegido", "Productos protegidos"));
//        Product existingProduct = productRepository.save(new Product(null, "Producto Protegido", "Desc", BigDecimal.valueOf(100.0), 10, category));
//
//        // ACT & ASSERT
//        mockMvc.perform(delete("/api/v1/products/{id}", existingProduct.getId()))
//                .andExpect(status().isForbidden()); // Espera 403 Forbidden
//    }
//}


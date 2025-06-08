package com.santicodev.gestorinventarioproductos.product.application.service;

import com.santicodev.gestorinventarioproductos.category.domain.Category;
import com.santicodev.gestorinventarioproductos.category.infraestructure.repository.CategoryRepository;
import com.santicodev.gestorinventarioproductos.product.domain.Product;
import com.santicodev.gestorinventarioproductos.common.infraestructure.dto.ProductDTO;
import com.santicodev.gestorinventarioproductos.product.infraestructure.repository.ProductRepository;
import com.santicodev.gestorinventarioproductos.common.domain.exception.DuplicateResourceException;
import com.santicodev.gestorinventarioproductos.common.domain.exception.ResourceNotFoundException;
import com.santicodev.gestorinventarioproductos.common.infraestructure.dto.ProductPartialUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// Define el nombre del caché para esta clase
@CacheConfig(cacheNames = "products")
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // --- Métodos de Mapeo (Entidad <-> DTO) ---
    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategory() != null ? product.getCategory().getId() : null
        );
        return dto;
    }

    // --- Métodos de Lógica de Negocio (CRUD) ---
    // Siempre ejecuta el metodo y actualiza la caché con el ID del producto creado
    @CachePut(key = "#result.id")
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        if (productRepository.existsByName(productDTO.name())) {
            throw new DuplicateResourceException("El producto con el nombre '" + productDTO.name() + "' ya existe.");
        }

        Category category = categoryRepository.findById(productDTO.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + productDTO.categoryId()));

        // 4. Mapeo de DTO a Entidad.
        Product product = new Product();
        product.setName(productDTO.name());
        product.setDescription(productDTO.description());
        product.setPrice(productDTO.price());
        product.setStock(productDTO.stock());
        product.setCategory(category); // Asigna el objeto Category completo.

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    // Cachea el resultado de este metodo La clave por defecto son los argumentos.
    @Cacheable
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        System.out.println("Fetching all products from DB...");
        return productRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Cachea el resultado usando el 'id' como clave
    @Cacheable(key = "#id")
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return mapToDTO(product);
    }

    // Siempre ejecuta el metodo y actualiza la caché con el 'id'
    @CachePut(key = "#id")
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        // 5. Lógica de Negocio: Evitar duplicados al actualizar.
        if (!existingProduct.getName().equals(productDTO.name()) && productRepository.existsByName(productDTO.name())) {
            throw new DuplicateResourceException("El producto con el nombre '" + productDTO.name() + "' ya existe.");
        }

        // 6. Validación de Relaciones: Asegurarse de que la categoría asociada exista (si cambia).
        Category category = categoryRepository.findById(productDTO.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + productDTO.categoryId()));

        // 7. Actualiza los campos.
        existingProduct.setName(productDTO.name());
        existingProduct.setDescription(productDTO.description());
        existingProduct.setPrice(productDTO.price());
        existingProduct.setStock(productDTO.stock());
        existingProduct.setCategory(category);

        Product updatedProduct = productRepository.save(existingProduct);
        return mapToDTO(updatedProduct);
    }

    // Elimina la entrada de la caché asociada con este 'id'
    @CacheEvict(key = "#id")
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + id);
        }
        productRepository.deleteById(id);
    }

    // Cachea el resultado de este metodo La clave por defecto son los argumentos.
    @Cacheable
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategoryId(Long categoryId) {
        // 8. Validación: Verificar que la categoría exista antes de buscar productos.
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Categoría no encontrada con ID: " + categoryId);
        }
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Siempre ejecuta el metodo y actualiza la caché con el 'id'
    @CachePut(key = "#id")
    @Transactional
    public ProductDTO patchProduct(Long id, ProductPartialUpdateDTO patchDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        // Aplicar cambios solo si los campos están presentes en el DTO de PATCH
        if (patchDTO.getName() != null && !patchDTO.getName().trim().isEmpty()) {
            // Lógica de Negocio: Evitar duplicados al actualizar.
            if (!existingProduct.getName().equals(patchDTO.getName()) && productRepository.existsByName(patchDTO.getName())) {
                throw new DuplicateResourceException("El producto con el nombre '" + patchDTO.getName() + "' ya existe.");
            }
            existingProduct.setName(patchDTO.getName());
        }

        if (patchDTO.getDescription() != null) {
            existingProduct.setDescription(patchDTO.getDescription());
        }

        if (patchDTO.getPrice() != null) {
            existingProduct.setPrice(patchDTO.getPrice());
        }

        if (patchDTO.getStock() != null) {
            existingProduct.setStock(patchDTO.getStock());
        }

        if (patchDTO.getCategoryId() != null) {
            // Validar si la nueva categoría existe
            Category category = categoryRepository.findById(patchDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + patchDTO.getCategoryId()));
            existingProduct.setCategory(category);
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return mapToDTO(updatedProduct);
    }
}

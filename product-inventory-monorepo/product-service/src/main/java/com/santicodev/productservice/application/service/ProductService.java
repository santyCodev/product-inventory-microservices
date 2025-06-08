package com.santicodev.productservice.application.service;

import com.santicodev.commondomainlib.domain.exception.DuplicateResourceException;
import com.santicodev.commondomainlib.domain.exception.ResourceNotFoundException;
import com.santicodev.commondomainlib.infraestructure.dto.CategoryDTO;
import com.santicodev.commondomainlib.infraestructure.dto.ProductDTO;
import com.santicodev.commondomainlib.infraestructure.dto.ProductPartialUpdateDTO;
import com.santicodev.productservice.client.CategoryServiceClient;
import com.santicodev.productservice.domain.Product;
import com.santicodev.productservice.infraestructure.repository.ProductRepository;
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
    private final CategoryServiceClient categoryServiceClient; // Inyecta el cliente Feign

    // --- Métodos de Mapeo (Entidad <-> DTO) ---
    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategoryId() != null ? product.getCategoryId() : null
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

        // --- CAMBIO CLAVE: Validación de Categoría vía FeignClient ---
        // Usamos categoryServiceClient para verificar si la categoría existe
        CategoryDTO categoryDetails = categoryServiceClient.getCategoryById(productDTO.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + productDTO.categoryId()));

        // 4. Mapeo de DTO a Entidad.
        Product product = new Product();
        product.setName(productDTO.name());
        product.setDescription(productDTO.description());
        product.setPrice(productDTO.price());
        product.setStock(productDTO.stock());
        product.setCategoryId(categoryDetails.id()); // Asigna el id de la categoria

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

        // Solo validar si el ID de categoría cambia
        if (!existingProduct.getCategoryId().equals(productDTO.categoryId())) {
            categoryServiceClient.getCategoryById(productDTO.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + productDTO.categoryId()));
        }

        // 7. Actualiza los campos.
        existingProduct.setName(productDTO.name());
        existingProduct.setDescription(productDTO.description());
        existingProduct.setPrice(productDTO.price());
        existingProduct.setStock(productDTO.stock());
        existingProduct.setCategoryId(productDTO.categoryId());

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

        // Validación: Verificar que la categoría exista antes de buscar productos.
        categoryServiceClient.getCategoryById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + categoryId));
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

        if (patchDTO.getCategoryId() != null && !existingProduct.getCategoryId().equals(patchDTO.getCategoryId())) {
            // Validar si la nueva categoría existe
            categoryServiceClient.getCategoryById(patchDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + patchDTO.getCategoryId()));
            existingProduct.setCategoryId(patchDTO.getCategoryId());
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return mapToDTO(updatedProduct);
    }
}

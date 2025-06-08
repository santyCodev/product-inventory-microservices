package com.santicodev.productservice.infraestructure.controller;

import com.santicodev.commondomainlib.infraestructure.dto.ProductDTO;
import com.santicodev.commondomainlib.infraestructure.dto.ProductPartialUpdateDTO;
import com.santicodev.productservice.application.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // Solo permitir a usuarios con rol ADMIN crear productos
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    // Permitir a usuarios con rol ADMIN o USER ver todos los productos
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // Permitir a usuarios con rol ADMIN o USER ver un producto específico
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    // Solo permitir a usuarios con rol ADMIN actualizar productos
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    // Solo permitir a usuarios con rol ADMIN eliminar productos
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Permitir a usuarios con rol ADMIN o USER poder filtrar productos por categoria
    @GetMapping("/category/{categoryId}") // 1. Nuevo endpoint para filtrar productos por ID de categoría.
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<ProductDTO>> getProductsByCategoryId(@PathVariable Long categoryId) {
        List<ProductDTO> products = productService.getProductsByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    // Solo permitir a usuarios con rol ADMIN actualizar parcialmente un producto
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> patchProduct(@PathVariable Long id, @Valid @RequestBody ProductPartialUpdateDTO patchDTO) {
        ProductDTO updatedProduct = productService.patchProduct(id, patchDTO);
        return ResponseEntity.ok(updatedProduct);
    }
}

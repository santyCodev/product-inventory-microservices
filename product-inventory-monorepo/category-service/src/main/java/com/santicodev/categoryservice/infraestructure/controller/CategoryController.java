package com.santicodev.categoryservice.infraestructure.controller;

import com.santicodev.categoryservice.application.service.CategoryService;
import com.santicodev.commondomainlib.infraestructure.dto.CategoryDTO;
import com.santicodev.commondomainlib.infraestructure.dto.CategoryPartialUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    // Mapea solicitudes POST a "/api/v1/categories".
    // Solo permitir a usuarios con rol ADMIN crear categorias
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    // Mapea solicitudes GET a "/api/v1/categories".
    // Permitir a usuarios con rol ADMIN o USER ver todas las categorias
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // Mapea solicitudes GET a "/api/v1/categories/{id}".
    // Permitir a usuarios con rol ADMIN o USER ver una categoria especifica
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    // {id}: Es una variable de ruta.
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    // Mapea solicitudes PUT a "/api/v1/categories/{id}".
    // Solo permitir a usuarios con rol ADMIN actualizar categorias
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    // Mapea solicitudes DELETE a "/api/v1/categories/{id}".
    // Solo permitir a usuarios con rol ADMIN eliminar categorias
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // Mapea solicitudes PATCH a "/api/v1/categories/{id}".
    // Solo permitir a usuarios con rol ADMIN actualizar parcialmente
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> patchCategory(@PathVariable Long id, @Valid @RequestBody CategoryPartialUpdateDTO patchDTO) {
        CategoryDTO updatedCategory = categoryService.patchCategory(id, patchDTO);
        return ResponseEntity.ok(updatedCategory);
    }
}

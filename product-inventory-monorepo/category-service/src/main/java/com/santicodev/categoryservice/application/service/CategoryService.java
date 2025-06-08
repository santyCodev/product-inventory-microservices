package com.santicodev.categoryservice.application.service;

import com.santicodev.categoryservice.domain.Category;
import com.santicodev.categoryservice.infraestructure.repository.CategoryRepository;
import com.santicodev.commondomainlib.domain.exception.DuplicateResourceException;
import com.santicodev.commondomainlib.domain.exception.ResourceNotFoundException;
import com.santicodev.commondomainlib.infraestructure.dto.CategoryDTO;
import com.santicodev.commondomainlib.infraestructure.dto.CategoryPartialUpdateDTO;
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
@CacheConfig(cacheNames = "categories")
public class CategoryService {
    private final CategoryRepository categoryRepository;

    // --- Métodos de Mapeo (Entidad <-> DTO) ---
    private CategoryDTO mapToDTO(Category category) {
        return new CategoryDTO(category.getId(), category.getName(), category.getDescription());
    }

    private Category mapToEntity(CategoryDTO categoryDTO) {
        return new Category(categoryDTO.id(), categoryDTO.name(), categoryDTO.description());
    }

    // --- Métodos de Lógica de Negocio (CRUD) ---

    @CachePut(key = "#result.id")
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.existsByName(categoryDTO.name())) {
            throw new DuplicateResourceException("La categoría con el nombre '" + categoryDTO.name() + "' ya existe.");
        }
        Category category = mapToEntity(categoryDTO);
        Category savedCategory = categoryRepository.save(category);
        return mapToDTO(savedCategory);
    }

    @Cacheable
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(key = "#id")
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        return mapToDTO(category);
    }

    @CachePut(key = "#id")
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        if (!existingCategory.getName().equals(categoryDTO.name()) && categoryRepository.existsByName(categoryDTO.name())) {
            throw new DuplicateResourceException("La categoría con el nombre '" + categoryDTO.name() + "' ya existe.");
        }

        existingCategory.setName(categoryDTO.name());
        existingCategory.setDescription(categoryDTO.description());

        Category updatedCategory = categoryRepository.save(existingCategory);
        return mapToDTO(updatedCategory);
    }

    @CacheEvict(key = "#id")
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada con ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @CachePut(key = "#id")
    @Transactional
    public CategoryDTO patchCategory(Long id, CategoryPartialUpdateDTO patchDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        // Aplicar cambios solo si los campos están presentes en el DTO de PATCH
        if (patchDTO.getName() != null && !patchDTO.getName().trim().isEmpty()) {
            if (!existingCategory.getName().equals(patchDTO.getName()) && categoryRepository.existsByName(patchDTO.getName())) {
                throw new DuplicateResourceException("La categoría con el nombre '" + patchDTO.getName() + "' ya existe.");
            }
            existingCategory.setName(patchDTO.getName());
        }

        if (patchDTO.getDescription() != null) {
            existingCategory.setDescription(patchDTO.getDescription());
        }

        Category updatedCategory = categoryRepository.save(existingCategory);
        return mapToDTO(updatedCategory);
    }
}

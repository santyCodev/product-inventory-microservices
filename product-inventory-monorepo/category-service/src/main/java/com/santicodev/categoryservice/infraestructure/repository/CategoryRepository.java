package com.santicodev.categoryservice.infraestructure.repository;

import com.santicodev.categoryservice.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Ver exlipcacion y anotaciones en el notion
 * - Proyecto: Gestor Inventario de Productos
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);
}

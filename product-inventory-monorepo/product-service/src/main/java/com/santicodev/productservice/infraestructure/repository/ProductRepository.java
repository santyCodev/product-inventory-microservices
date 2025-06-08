package com.santicodev.productservice.infraestructure.repository;

import com.santicodev.productservice.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Ver exlipcacion y anotaciones en el notion
 * - Proyecto: Gestor Inventario de Productos
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);
    List<Product> findByCategoryId(Long categoryId);
}

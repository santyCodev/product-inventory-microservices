package com.santicodev.productservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Ver exlipcacion y anotaciones en el notion
 * - Proyecto: Gestor Inventario de Productos
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock; // Cantidad de productos en stock

    // --- CAMBIOS PARA MICROSERVICIOS ---
    // verlo en apuntes
    @Column(nullable = false)
    private Long categoryId;
}

package com.santicodev.gestorinventarioproductos.security.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles") // Nombre de la tabla en la base de datos
public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usaremos un Enum o String para el nombre del rol, con convención de Spring Security (ROLE_...)
    @Column(length = 20, unique = true, nullable = false)
    private String name;

}

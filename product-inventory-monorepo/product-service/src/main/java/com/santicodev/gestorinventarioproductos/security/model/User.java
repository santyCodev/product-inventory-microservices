package com.santicodev.gestorinventarioproductos.security.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Útil para construir objetos User en el registro
@Entity
@Table(name = "users", // Nombre de la tabla en la base de datos
        uniqueConstraints = { // Asegura que username y email sean únicos
                @UniqueConstraint(columnNames = "username")
        })
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; // ¡Almacenaremos la contraseña HASHED!

    // Relación Many-to-Many con Role
    @ManyToMany(fetch = FetchType.EAGER) // Carga los roles inmediatamente cuando se carga el usuario
    @JoinTable(name = "user_roles", // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "user_id"), // Columna para User en la tabla intermedia
            inverseJoinColumns = @JoinColumn(name = "role_id")) // Columna para Role en la tabla intermedia
    private Set<Role> roles = new HashSet<>(); // Usa un HashSet para evitar duplicados y mejor rendimiento

    // Principio SOLID: Responsabilidad Única. Esta entidad solo representa un usuario.
    // DDD: User como Aggregate Root, con Roles como Value Objects o entidades menores dentro de su contexto.
}

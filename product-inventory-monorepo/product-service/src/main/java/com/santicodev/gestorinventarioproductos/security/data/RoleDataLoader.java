package com.santicodev.gestorinventarioproductos.security.data;

import com.santicodev.gestorinventarioproductos.security.model.Role;
import com.santicodev.gestorinventarioproductos.security.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

// Indica que esta clase contiene definiciones de beans
@Configuration
public class RoleDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(RoleDataLoader.class);

    // Definición de un bean CommandLineRunner para carga de roles
    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            logger.info("Verificando y precargando roles en la base de datos...");

            // Crea o encuentra el rol ROLE_USER
            createRoleIfNotFound(roleRepository, "ROLE_USER");

            // Crea o encuentra el rol ROLE_ADMIN
            createRoleIfNotFound(roleRepository, "ROLE_ADMIN");

            logger.info("Roles precargados exitosamente.");
        };
    }

    private void createRoleIfNotFound(RoleRepository roleRepository, String roleName) {
        Optional<Role> existingRole = roleRepository.findByName(roleName);
        if (existingRole.isEmpty()) {
            Role newRole = new Role();
            newRole.setName(roleName);
            roleRepository.save(newRole);
            logger.info("Rol '{}' creado.", roleName);
        } else {
            logger.info("Rol '{}' ya existe.", roleName);
        }
    }
}

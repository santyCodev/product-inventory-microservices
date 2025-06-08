package com.santicodev.gestorinventarioproductos.security.controller;

import com.santicodev.gestorinventarioproductos.security.Service.UserDetailsImpl;
import com.santicodev.gestorinventarioproductos.security.jwt.JwtUtils;
import com.santicodev.gestorinventarioproductos.security.model.Role;
import com.santicodev.gestorinventarioproductos.security.model.User;
import com.santicodev.gestorinventarioproductos.common.security.payload.request.LoginRequest;
import com.santicodev.gestorinventarioproductos.common.security.payload.request.RegisterRequest;
import com.santicodev.gestorinventarioproductos.common.security.payload.response.JwtResponse;
import com.santicodev.gestorinventarioproductos.common.security.payload.response.MessageResponse;
import com.santicodev.gestorinventarioproductos.security.repository.RoleRepository;
import com.santicodev.gestorinventarioproductos.security.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Permite CORS desde cualquier origen PARA DESARROLLO ONLY
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // Inyección de dependencias a través del constructor (mejor práctica de Clean Code y SOLID)
    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    // Endpoint de inicio de sesión
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // Autentica al usuario usando el AuthenticationManager de Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        // Establece la autenticación en el SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Genera el token JWT
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Obtiene los detalles del usuario autenticado para la respuesta
        // Castea a tu clase
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                // Ahora puedes obtener el ID directamente
                userDetails.getId(),
                userDetails.getUsername(),
                roles));
    }

    // Endpoint de registro
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {

        // Verifica si el username ya existe
        if (userRepository.existsByUsername(registerRequest.username())) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse("Error: ¡El nombre de usuario ya está en uso!"));
        }

        // Crea un nuevo usuario
        User user = new User();
        user.setUsername(registerRequest.username());
        user.setPassword(passwordEncoder.encode(registerRequest.password())); // Hashea la contraseña

        // Asigna roles al usuario. Por defecto, asignaremos ROLE_USER.
        // Para usuarios avanzados, podrías tener una lógica para asignar ROLE_ADMIN
        // por ejemplo, si es el primer usuario o a través de un endpoint separado y protegido.
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Rol 'ROLE_USER' no encontrado."));
        roles.add(userRole);
        user.setRoles(roles);

        // Guarda el usuario en la base de datos
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("¡Usuario registrado exitosamente!"));
    }

    // Leer CONSIDERACIÓN IMPORTANTE PARA EL Casteo en authenticateUser: ver apuntes
}

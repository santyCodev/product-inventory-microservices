package com.santicodev.gestorinventarioproductos.security.jwt;

import com.santicodev.gestorinventarioproductos.security.Service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Marca la clase como un componente de Spring
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService; // Nuestro servicio para cargar usuarios

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Obtener el token JWT de la cabecera de la petición
            String jwt = parseJwt(request);

            // 2. Si el token existe y es válido
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // 3. Obtener el nombre de usuario del token
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                // 4. Cargar los detalles del usuario (roles, password hasheado)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. Crear un objeto de autenticación
                // Este objeto representa un usuario autenticado en Spring Security
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // La contraseña ya no es necesaria aquí, ya fue verificada por el token
                        userDetails.getAuthorities()); // Roles/Autoridades del usuario

                // 6. Establecer los detalles de la petición (IP, sesión)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. Establecer la autenticación en el SecurityContext de Spring
                // Esto permite que Spring Security sepa quién es el usuario autenticado
                // en cualquier parte del código durante el procesamiento de esta petición.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("No se pudo establecer la autenticación del usuario: {}", e.getMessage());
        }

        // 8. Continuar la cadena de filtros de Spring Security
        filterChain.doFilter(request, response);
    }

    // Método auxiliar para extraer el token de la cabecera "Authorization"
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        // Los tokens JWT suelen venir en el formato "Bearer <TOKEN>"
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Extrae el token sin "Bearer "
        }
        return null;
    }
}

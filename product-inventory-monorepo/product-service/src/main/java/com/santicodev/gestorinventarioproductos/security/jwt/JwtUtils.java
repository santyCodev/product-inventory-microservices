package com.santicodev.gestorinventarioproductos.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Inyecta la clave secreta desde application.properties
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    // Inyecta el tiempo de expiración desde application.properties
    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // Método para generar un token JWT
    public String generateJwtToken(Authentication authentication) {
        // Obtiene el UserDetails del objeto Authentication
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        // Constructor del JWT: Emisor, fecha de emisión, fecha de expiración, sujeto (username), y firma.
        return Jwts.builder()
                .setSubject((userPrincipal.getUsername())) // El "subject" del token es el nombre de usuario
                .setIssuedAt(new Date()) // Fecha de emisión del token
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Fecha de expiración
                .signWith(key(), SignatureAlgorithm.HS512) // Firma el token con nuestra clave secreta y algoritmo HS512
                .compact(); // Construye y compacta el token en una cadena JWT
    }

    // Método para obtener la clave de firma a partir del secreto
    private Key key() {
        // Decodifica la clave secreta base64 y la convierte en una clave segura
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Método para obtener el nombre de usuario de un token JWT
    public String getUserNameFromJwtToken(String token) {
        // Parsea el token y extrae las claims
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject(); // Obtiene el sujeto (username)
    }

    // Método para validar un token JWT
    public boolean validateJwtToken(String authToken) {
        try {
            // Intenta parsear y verificar el token con la clave secreta
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true; // Si no hay excepción, el token es válido
        } catch (MalformedJwtException e) {
            logger.error("Token JWT inválido: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("La cadena JWT está vacía: {}", e.getMessage());
        }
        return false; // Si alguna excepción ocurre, el token es inválido
    }
}

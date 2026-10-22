package com.alexlizzt.inventory_service.infraestructure.web.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuración de seguridad para las pruebas de slice web.
 *
 * Se declara como {@link TestConfiguration} para que el component scan del
 * contexto completo ({@code @SpringBootTest}) la ignore: de lo contrario su
 * filter chain "any request" dejaría inalcanzable la de producción. Las pruebas
 * de slice la registran con {@code @Import}.
 */
@TestConfiguration
@EnableMethodSecurity
public class TestSecurityConfig {
    @Bean
    SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception

                // Usuario NO autenticado -> 401
                .authenticationEntryPoint(
                    (request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                )

                // Usuario autenticado pero sin permisos -> 403
                .accessDeniedHandler(
                    (request, response, accessDeniedException) ->
                        response.sendError(HttpServletResponse.SC_FORBIDDEN)
                )
            );

        return http.build();
    }
}

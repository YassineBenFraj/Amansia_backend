package com.amansia.amansiabackend.config;

import com.amansia.amansiabackend.security.JwtRequestFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    
    @Bean
        public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtRequestFilter jwtRequestFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) ->
                    writeSecurityError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Authentification requise"))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    writeSecurityError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Acces interdit")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login",
                                "/api/auth/mot-de-passe-oublie", "/api/auth/reinitialiser-mot-de-passe").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/commandes/admin", "/api/commandes/admin/**", "/api/paiements/admin", "/api/paiements/admin/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers("/api/avis/admin").hasRole("ADMINISTRATEUR")
                        .requestMatchers("/api/stocks/admin", "/api/stocks/admin/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers("/api/adresses/**").authenticated()
                        .requestMatchers("/api/notifications", "/api/notifications/**").authenticated()
                        .requestMatchers("/api/admin/promotions", "/api/admin/promotions/**", "/api/admin/codes-promo", "/api/admin/codes-promo/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers("/api/codes-promo/valider").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET,
                            "/api/categories/actives", "/api/produits", "/api/produits/recherche", "/api/produits/bornes-prix",
                            "/api/produits/{id:[0-9]+}", "/api/produits/{id:[0-9]+}/similaires", "/api/produits/categorie/{id}/actifs",
                            "/api/images-produits", "/api/images-produits/{id:[0-9]+}", "/api/images-produits/produit/{id}",
                            "/api/materiaux/actifs", "/api/couleurs/actives",
                            "/api/variantes-produits/produit/{id}/actives", "/api/avis/produit/{id}", "/api/stocks/produit/**").permitAll()
                        .requestMatchers("/uploads/produits/**", "/uploads/categories/**", "/uploads/profils/**").permitAll()
                        .requestMatchers("/api/utilisateurs/me", "/api/utilisateurs/me/**").hasRole("CLIENT")
                        .requestMatchers("/api/utilisateurs/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.GET, "/api/produits/admin").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.GET, "/api/produits/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/produits").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/produits/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/produits/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.GET, "/api/materiaux/**").hasAnyRole("CLIENT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.POST, "/api/materiaux").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/materiaux/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/materiaux/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.GET, "/api/couleurs/**").hasAnyRole("CLIENT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.POST, "/api/couleurs").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/couleurs/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/couleurs/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.GET, "/api/variantes-produits/**").hasAnyRole("CLIENT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.POST, "/api/variantes-produits").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/variantes-produits/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/variantes-produits/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers("/api/favoris", "/api/favoris/**").hasRole("CLIENT")
                        .requestMatchers("/api/panier/**").authenticated()
                        .requestMatchers("/api/commandes/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/avis/*/approbation").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.POST, "/api/avis").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/avis/**").hasAnyRole("CLIENT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/avis/**").hasAnyRole("CLIENT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/avis/**").hasAnyRole("CLIENT", "ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.POST, "/api/paiements/webhook").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/paiements/**").hasAnyRole("CLIENT", "ADMINISTRATEUR")
                        .requestMatchers("/api/paiements/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/images-produits/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/images-produits/upload").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.PUT, "/api/images-produits/**").hasRole("ADMINISTRATEUR")
                        .requestMatchers(HttpMethod.DELETE, "/api/images-produits/**").hasRole("ADMINISTRATEUR")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private void writeSecurityError(
            HttpServletResponse response,
            int status,
            String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String error = status == HttpServletResponse.SC_UNAUTHORIZED ? "Unauthorized" : "Forbidden";
        response.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now()
            + "\",\"status\":" + status
            + ",\"error\":\"" + error
            + "\",\"message\":\"" + message + "\"}");
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "http://127.0.0.1:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

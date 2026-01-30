package dh.tour.config;

import dh.tour.config.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // Si usas el frontend, después ajustamos esto más a fondo
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Endpoints Públicos
                        .requestMatchers("/", "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/tours/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // IMPORTANTE para evitar 403 en navegadores

                        // 2. Reglas de ADMIN/SUPER_ADMIN (Quitamos el comentario y usamos nombres limpios)
                        // Usamos hasAnyRole que internamente busca "ROLE_" + el nombre
                        .requestMatchers(HttpMethod.POST, "/tours").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/tours/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/tours/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tours/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/usuarios").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // 3. Reglas de Usuario común o cualquier logueado
                        .requestMatchers(HttpMethod.POST, "/tours/*/inscribir").authenticated()
                        .requestMatchers("/usuarios/**").authenticated()

                        // 4. Todo lo demás requiere login
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

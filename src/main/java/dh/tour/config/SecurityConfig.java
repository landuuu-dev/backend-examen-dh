package dh.tour.config;

import dh.tour.config.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/auth/**", "/usuarios").permitAll()

                        // Usuarios logueados pueden actualizar su cuenta
                        .requestMatchers("/usuarios/*").authenticated()  // PUT /usuarios/{id}

                        // Favoritos: usuarios logueados pueden acceder
                        .requestMatchers("/usuarios/*/favoritos/**").authenticated()  // POST y DELETE favoritos

                        // Solo admins pueden GET y DELETE todos los usuarios
                        .requestMatchers("/usuarios").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        // Cualquier otro endpoint requiere autenticación
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

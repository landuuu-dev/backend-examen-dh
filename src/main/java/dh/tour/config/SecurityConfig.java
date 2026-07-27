package dh.tour.config;

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
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Público básico
                        .requestMatchers("/", "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Ruta de administración
                        .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")

                        // 3. Inscripciones y detalles protegidos en Tours
                        .requestMatchers(HttpMethod.POST, "/tours/*/inscribir").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/tours/*/desinscribirse").authenticated()
                        .requestMatchers(HttpMethod.GET, "/tours/*/inscritos").hasAnyAuthority("ADMIN", "SUPER_ADMIN", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")

                        // 4. GETs Públicos (Resto de Tours y Categorías)
                        .requestMatchers(HttpMethod.GET, "/tours/**", "/categorias/**").permitAll()

                        // 5. POST/PUT/PATCH/DELETE Protegidos
                        .requestMatchers(HttpMethod.POST, "/categorias/**", "/tours/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categorias/**", "/tours/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/categorias/**", "/tours/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/categorias/**", "/tours/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")

                        // 6. Usuarios autenticados
                        .requestMatchers("/usuarios/**").authenticated()

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
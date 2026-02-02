package dh.tour.config;

import dh.tour.config.security.CustomUserDetails;
import dh.tour.model.Usuario;
import dh.tour.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final StringRedisTemplate redisTemplate; // <--- 2. Declarar Redis

    public JwtFilter(JwtUtil jwtUtil, UsuarioRepository usuarioRepository, StringRedisTemplate redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {

                if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist_token:" + token))) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Token revocado\", \"mensaje\": \"La sesión ha sido cerrada\"}");
                    return; // Detiene el filtro y no deja pasar al controlador
                }


                Claims claims = jwtUtil.getClaims(token);
                String correo = claims.getSubject();

                // Buscamos el usuario real en DB
                Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();

                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())
                    );

                    // ⚡ Aquí usamos tu CustomUserDetails
                    CustomUserDetails principal = new CustomUserDetails(usuario, authorities);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    authorities
                            );

                    System.out.println("DEBUG - USUARIO: " + principal.getUsername() + " | ROLES: " + authorities);

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            } // ... dentro de doFilterInternal ...

         catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 1. EXTRAER LA URI Y EL MÉTODO
            String path = request.getRequestURI();
            String method = request.getMethod();

            // 2. ¿ES UNA RUTA PÚBLICA? (Tours o Categorías en GET)
            boolean esRutaPublica = (path.contains("/tours") || path.contains("/categorias"))
                    && method.equals("GET");

            if (esRutaPublica) {
                // Ignoramos el token vencido y dejamos que siga como invitado
                filterChain.doFilter(request, response);
                return;
            }

            // 3. SI NO ES PÚBLICA, AHÍ SÍ LANZAMOS EL ERROR
            escribirError(response, "Token expirado", "Tu sesión ha vencido, por favor inicia sesión de nuevo", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (Exception e) {
            System.out.println("ERROR EN JWT FILTER: " + e.getMessage());
            SecurityContextHolder.clearContext();
            escribirError(response, "Error de autenticación", e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
            return;
        }
    }

    filterChain.doFilter(request, response);
}

// Métodito auxiliar para que el JSON sea "lindo" y ordenado
private void escribirError(HttpServletResponse response, String mensaje, String detalle, int status) throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");

    // Formato exacto de tu GlobalExceptionHandler
    String json = String.format(
            "{\"statusCode\": %d, \"timestamp\": \"%s\", \"message\": \"%s\", \"description\": \"%s\"}",
            status, java.time.LocalDateTime.now(), mensaje, detalle
    );

    response.getWriter().write(json);
}}
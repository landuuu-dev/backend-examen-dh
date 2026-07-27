package dh.tour.config;

import dh.tour.config.security.CustomUserDetails;
import dh.tour.model.Usuario;
import dh.tour.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final StringRedisTemplate redisTemplate;

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
                // 1. Validar Blacklist en Redis
                if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist_token:" + token))) {
                    System.out.println("⚠️ JWT RECHAZADO: Token en blacklist Redis");
                    escribirError(response, "Token revocado", "La sesión ha sido cerrada", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                // 2. Extraer datos del token
                Claims claims = jwtUtil.getClaims(token);
                String correo = claims.getSubject();
                System.out.println("🔍 JWT FILTER - Correo extraído: " + correo);

                // 3. Buscar usuario en base de datos
                Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();

                    String nombreRol = usuario.getRol().name();
                    String rolConPrefix = nombreRol.startsWith("ROLE_") ? nombreRol : "ROLE_" + nombreRol;
                    String rolSinPrefix = nombreRol.startsWith("ROLE_") ? nombreRol.substring(5) : nombreRol;

                    // Mapeamos ambas variantes explícitas
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority(rolConPrefix),
                            new SimpleGrantedAuthority(rolSinPrefix)
                    );

                    CustomUserDetails principal = new CustomUserDetails(usuario, authorities);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(auth);

                    System.out.println("✅ JWT AUTENTICADO -> Usuario: " + correo + " | Authorities: " + authorities);
                } else {
                    System.out.println("❌ JWT ERROR: Usuario no encontrado en BD: " + correo);
                }

            } catch (ExpiredJwtException e) {
                String path = request.getRequestURI();
                String method = request.getMethod();

                boolean esRutaPublica = (path.contains("/tours") || path.contains("/categorias")) && method.equals("GET");

                if (esRutaPublica) {
                    filterChain.doFilter(request, response);
                    return;
                }

                escribirError(response, "Token expirado", "Tu sesión ha vencido, por favor inicia sesión de nuevo", HttpServletResponse.SC_UNAUTHORIZED);
                return;

            } catch (Exception e) {
                System.out.println("💥 ERROR EN JWT FILTER: " + e.getMessage());
                SecurityContextHolder.clearContext();
                escribirError(response, "Error de autenticación", e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } else {
            System.out.println("ℹ️ INFO: Petición a [" + request.getRequestURI() + "] sin Header Authorization.");
        }

        filterChain.doFilter(request, response);
    }

    private void escribirError(HttpServletResponse response, String mensaje, String detalle, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");

        String json = String.format(
                "{\"statusCode\": %d, \"timestamp\": \"%s\", \"message\": \"%s\", \"description\": \"%s\"}",
                status, java.time.LocalDateTime.now(), mensaje, detalle
        );

        response.getWriter().write(json);
    }
}
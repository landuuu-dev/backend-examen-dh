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

            }  catch (Exception e) {
            System.out.println("ERROR EN JWT FILTER: " + e.getMessage());
            e.printStackTrace(); // Esto te dirá la línea exacta del fallo
            SecurityContextHolder.clearContext();
        }
        }

        filterChain.doFilter(request, response);
    }
}

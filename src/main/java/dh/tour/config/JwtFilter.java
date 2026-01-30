package dh.tour.config;

import dh.tour.config.security.CustomUserDetails;
import dh.tour.model.Usuario;
import dh.tour.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    public JwtFilter(JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
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

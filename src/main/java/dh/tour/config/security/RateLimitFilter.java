package dh.tour.config.security;

import org.springframework.stereotype.Component;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    // Cubeta principal: 10 peticiones por minuto
    private final Bucket bucket = Bucket4j.builder()
            .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
            .build();

    // "La Cárcel": Guardamos la IP y el momento en que termina su castigo
    private final Map<String, LocalDateTime> blacklist = new ConcurrentHashMap<>();
    private final Duration TIEMPO_CASTIGO = Duration.ofMinutes(20);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Obtenemos la IP del que llama
        String clientIp = httpRequest.getRemoteAddr();

        // Verificamos si la IP está en la cárcel
        if (blacklist.containsKey(clientIp)) {
            if (LocalDateTime.now().isBefore(blacklist.get(clientIp))) {
                httpResponse.setStatus(403); // Forbidden
                httpResponse.getWriter().write("🚫 Estas bloqueado temporalmente por abuso. Intenta en 5 minutos.");
                return;
            } else {
                blacklist.remove(clientIp); // Ya cumplió su condena
            }
        }

        String origin = httpRequest.getHeader("Origin");
        boolean esMiFront = (origin != null && origin.contains("localhost:5173"));

        if (!esMiFront) {
            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                // Si agota los tokens, lo mandamos a la cárcel
                blacklist.put(clientIp, LocalDateTime.now().plus(TIEMPO_CASTIGO));
                httpResponse.setStatus(429);
                httpResponse.getWriter().write("🚫 Abuso detectado. Tu dispositivo ha sido bloqueado por 5 minutos.");
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
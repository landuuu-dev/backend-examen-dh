package dh.tour.config.security; // Asegúrate que el paquete sea el correcto

import io.github.bucket4j.*;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter implements Filter {
    @Value("${app.frontend.url}")
    private String frontendUrl;

    // Cubeta de tokens (en memoria para velocidad)
    private final Bucket bucket = Bucket4j.builder()
            .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
            .build();

    private final StringRedisTemplate redisTemplate;

    // Spring inyecta la conexión automáticamente
    public RateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. CONFIGURACIÓN DE CORS (Debe ir primero)
        httpResponse.setHeader("Access-Control-Allow-Origin", frontendUrl);
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Origin, Accept");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        // 2. RESPONDER AL PREFLIGHT (Fundamental para que React no falle)
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String clientIp = httpRequest.getRemoteAddr();
        String redisKey = "blacklist:" + clientIp;

        // 3. VERIFICAR BLACKLIST EN REDIS
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            String jsonResponse = "{\"error\": \"Bloqueo de seguridad\", \"mensaje\": \"Tu IP esta bloqueada en REDIS por 700 minutos\"}";
            httpResponse.getWriter().write(jsonResponse);
            httpResponse.getWriter().flush();
            return; // Detenemos la ejecución aquí
        }

        // 4. LÓGICA DE EXCEPCIÓN PARA TU FRONT-END
        String origin = httpRequest.getHeader("Origin");
        boolean esMiFront = (origin != null && origin.contains(frontendUrl));

        if (esMiFront) {
            chain.doFilter(request, response); // Pasa libre
        } else {
            // 5. RATE LIMIT PARA EL RESTO (Consola, Postman, Bots)
            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                redisTemplate.opsForValue().set(redisKey, "banned", 700, TimeUnit.MINUTES);
                httpResponse.setStatus(429);
                httpResponse.setContentType("text/plain; charset=UTF-8");
                httpResponse.getWriter().write("🚫 Límite excedido. Bloqueado en REDIS por 700 minutos.");
                httpResponse.getWriter().flush();
            }
        }
    }
}
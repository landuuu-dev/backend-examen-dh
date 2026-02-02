package dh.tour.controllers;

import dh.tour.config.JwtUtil;
import dh.tour.dto.request.ForgotPasswordRequest;
import dh.tour.dto.request.LoginRequest;
import dh.tour.dto.request.RegisterRequest;
import dh.tour.dto.request.ResetPasswordRequest;
import dh.tour.dto.response.MensajeResponse;
import dh.tour.dto.response.UsuarioResponse;
import dh.tour.model.Rol;
import dh.tour.model.Usuario;
import dh.tour.service.EmailService;
import dh.tour.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final RedisTemplate redisTemplate;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Usuario usuario = usuarioService.login(
                request.getCorreo(),
                request.getPassword()
        );

        String token = jwtUtil.generateToken(usuario);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "rol", usuario.getRol()
        ));
    }

    // En AuthController.java

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest request) {
        // 1. Mapeamos el Request a la Entidad
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(request.getPassword());
        usuario.setRol(Rol.USER);

        // 2. El servicio se encarga del hashing
        Usuario creado = usuarioService.registrar(usuario);

        // 3. Enviamos el email
        emailService.enviarEmailBienvenida(creado.getCorreo(), creado.getNombre());

        // 4. TRANSFORMAMOS A RESPONSE (Para no enviar el password al frontend)
        UsuarioResponse response = UsuarioResponse.builder()
                .id(creado.getId())
                .nombre(creado.getNombre())
                .correo(creado.getCorreo())
                .rol(creado.getRol())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MensajeResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        usuarioService.solicitarRecuperacion(request.getCorreo());
        return ResponseEntity.ok(new MensajeResponse("Email de recuperación enviado"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MensajeResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        usuarioService.completarRecuperacion(request.getToken(), request.getPassword());
        return ResponseEntity.ok(new MensajeResponse("Contraseña actualizada con éxito"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // Calculamos cuánto tiempo le queda de vida al token
            long remainingTime = jwtUtil.getRemainingTime(token);

            // Lo guardamos en Redis con ese tiempo de vida
            redisTemplate.opsForValue().set("blacklist_token:" + token, "revoked", remainingTime, TimeUnit.MILLISECONDS);

            return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada con éxito"));
        }
        return ResponseEntity.badRequest().body("Token no proporcionado");
    }


}

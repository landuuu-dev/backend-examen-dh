package dh.tour.service;
import dh.tour.config.JwtUtil;
import dh.tour.dto.response.UsuarioResponse;
import dh.tour.exceptions.OperationNotAllowedException;
import dh.tour.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import dh.tour.model.Rol;
import dh.tour.model.Tour;
import dh.tour.model.Usuario;
import dh.tour.repository.TourRepository;
import dh.tour.repository.UsuarioRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final TourRepository tourRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final RedisTemplate <String, Object> redisTemplate;

    public Usuario registrar(Usuario usuario) {
        try {
            // Hashing de seguridad
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

            // Inicializar favoritos para evitar NullPointerException
            if (usuario.getFavoritos() == null) {
                usuario.setFavoritos(new ArrayList<>());
            }

            return usuarioRepository.save(usuario);
        } catch (DuplicateKeyException e) {
            throw new ResourceNotFoundException("El usuario con este correo ya está registrado.");
        }
    }

    public Usuario login(String correo, String password) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new ResourceNotFoundException("Credenciales incorrectas");
        }

        return usuario;
    }

    public void solicitarRecuperacion(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Correo no encontrado"));

        String token = UUID.randomUUID().toString();
        usuario.setResetToken(token);
        usuario.setResetTokenExpiration(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);

        emailService.enviarEmailRecuperacion(correo, token);
    }

    public void completarRecuperacion(String token, String nuevaPassword) {
        // Buscamos directamente en MongoDB por el campo resetToken
        Usuario usuario = usuarioRepository.findByResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token inválido o no encontrado"));

        if (usuario.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new ResourceNotFoundException("El token ha expirado");
        }

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiration(null);
        usuarioRepository.save(usuario);
    }

    public void promoteToAdmin(String userId) {

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getRol() == Rol.SUPER_ADMIN) {
            throw new ResourceNotFoundException("No se puede modificar al SUPER_ADMIN");
        }

        usuario.setRol(Rol.ADMIN);
        usuarioRepository.save(usuario);
    }

    public Usuario actualizar(String id, Usuario usuarioActualizado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setNombre(usuarioActualizado.getNombre());
        usuario.setCorreo(usuarioActualizado.getCorreo());

        // Solo actualizar contraseña si viene nueva
        if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
        }

        return usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getRol() == Rol.SUPER_ADMIN) {
            throw new OperationNotAllowedException("No se puede eliminar a un SUPER_ADMIN");
        }

        usuarioRepository.deleteById(id);
    }
    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correo));
    }

    public Usuario buscarPorId(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }


    public Usuario agregarFavorito(String usuarioId, String tourId) {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            if (!usuario.getFavoritos().contains(tourId)) {
                usuario.getFavoritos().add(tourId);
            }

            return usuarioRepository.save(usuario);
        }

        public Usuario quitarFavorito(String usuarioId, String tourId) {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            usuario.getFavoritos().remove(tourId);

            return usuarioRepository.save(usuario);
        }

    // En dh.tour.service.UsuarioService
    public List<Tour> obtenerFavoritos(String usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (usuario.getFavoritos() == null || usuario.getFavoritos().isEmpty()) {
            return List.of();
        }

        // 1. Buscamos los tours reales en la DB usando los IDs del usuario
        List<Tour> toursExistentes = tourRepository.findAllById(usuario.getFavoritos());

        // 2. Si la lista de tours encontrados es más corta que la de IDs guardados...
        if (toursExistentes.size() < usuario.getFavoritos().size()) {
            // Limpiamos la lista del usuario dejando solo los que sí existen
            List<String> idsLimpios = toursExistentes.stream()
                    .map(Tour::getId)
                    .toList();
            usuario.setFavoritos(new java.util.ArrayList<>(idsLimpios));
            usuarioRepository.save(usuario);
            System.out.println("🧹 Favoritos limpiados: se eliminaron IDs de tours borrados.");
        }

        return toursExistentes;
    }

    // En dh.tour.service.UsuarioService.java agrega:

    public Usuario actualizarParcial(String id, Map<String, Object> campos) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        campos.forEach((llave, valor) -> {
            switch (llave) {
                case "nombre":
                    usuario.setNombre((String) valor);
                    break;
                case "correo":
                    usuario.setCorreo((String) valor);
                    break;
                case "password":
                    if (valor != null && !((String) valor).isEmpty()) {
                        usuario.setPassword(passwordEncoder.encode((String) valor));
                    }
                    break;
                // No permitimos cambiar el ROL por aquí por seguridad
            }
        });

        return usuarioRepository.save(usuario);
    }

    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(u -> UsuarioResponse.builder()
                        .id(u.getId())
                        .nombre(u.getNombre())
                        .correo(u.getCorreo())
                        .rol(u.getRol())
                        .build())
                .toList();
    }

    public void logout(String token) {
        // Extraemos el tiempo que le queda de vida al token para no guardarlo en Redis eternamente
        long remainingTime = jwtUtil.getRemainingTime(token);
        if (remainingTime > 0) {
            // Guardamos el token en Redis con el prefijo "blacklist_token:"
            redisTemplate.opsForValue().set("blacklist_token:" + token, "revoked", remainingTime, TimeUnit.MILLISECONDS);
        }
    }

}





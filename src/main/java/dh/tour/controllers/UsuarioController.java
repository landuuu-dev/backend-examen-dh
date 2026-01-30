package dh.tour.controllers;
import dh.tour.config.security.CustomUserDetails;
import dh.tour.dto.UsuarioResponse;
import dh.tour.model.Tour;
import dh.tour.model.Usuario;
import dh.tour.repository.InscripcionRepository;
import dh.tour.repository.UsuarioRepository;
import dh.tour.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final InscripcionRepository inscripcionRepository;

    public UsuarioController(UsuarioRepository usuarioRepository, UsuarioService usuarioService, InscripcionRepository inscripcionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.inscripcionRepository = inscripcionRepository;
    }

    // 🔹 Obtener todos los usuarios
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping
    public List<Usuario> getAll() {
        return usuarioRepository.findAll();
    }


    // 🔹 Obtener usuario por ID
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping("/{id}")
    public Optional<Usuario> getById(@PathVariable String id) {
        return usuarioRepository.findById(id);
    }


    // 🔹 Actualizar usuario
    // 🔹 Actualizar usuario
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // solo verifica que esté autenticado
    public ResponseEntity<String> actualizarUsuario(
            @PathVariable String id,
            @RequestBody UsuarioResponse usuarioResponse,
            @AuthenticationPrincipal CustomUserDetails principal) {

        // 🔹 Validación: solo puede actualizar su propio usuario
        if (!principal.getId().equals(id)) {
            return ResponseEntity.status(403).body("No autorizado para actualizar este usuario");
        }

        // 🔹 Buscar usuario en DB
        Usuario usuario = usuarioService.buscarPorId(id);

        // 🔹 Actualizar campos permitidos
        usuario.setNombre(usuarioResponse.getNombre());
        usuario.setCorreo(usuarioResponse.getCorreo()); // opcional, si quieres permitir cambio de correo

        // 🔹 Guardar cambios
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Usuario actualizado correctamente");
    }

    // 🔹 Eliminar usuario
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    // 🔹 crear favoritos
    @PostMapping("/{id}/favoritos/{tourId}")
    public ResponseEntity<Usuario> agregarFavorito(
            @PathVariable String id,
            @PathVariable String tourId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getId().equals(id)) {
            return ResponseEntity.status(403).build();
        }

        Usuario usuarioActualizado = usuarioService.agregarFavorito(id, tourId);
        return ResponseEntity.ok(usuarioActualizado);
    }

    // 🔹 eliminar favorito
    @DeleteMapping("/{id}/favoritos/{tourId}")
    public ResponseEntity<Usuario> quitarFavorito(
            @PathVariable String id,
            @PathVariable String tourId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getId().equals(id)) {
            return ResponseEntity.status(403).build();
        }

        Usuario usuarioActualizado = usuarioService.quitarFavorito(id, tourId);
        return ResponseEntity.ok(usuarioActualizado);
    }

    // 🔹 Listar tours favoritos del usuario
    @GetMapping("/{id}/favoritos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Tour>> listarFavoritos(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getId().equals(id)) {
            return ResponseEntity.status(403).build();
        }

        List<Tour> favoritos = usuarioService.obtenerFavoritos(id);
        return ResponseEntity.ok(favoritos);
    }

    @GetMapping("/{id}/mis-inscripciones")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> misInscripciones(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        // Seguridad: El usuario solo puede ver sus propias inscripciones
        if (!principal.getId().equals(id)) {
            return ResponseEntity.status(403).body("No puedes ver inscripciones de otros");
        }

        return ResponseEntity.ok(inscripcionRepository.findByUsuarioId(id));
    }


}


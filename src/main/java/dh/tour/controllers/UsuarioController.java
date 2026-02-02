package dh.tour.controllers;
import dh.tour.config.security.CustomUserDetails;
import dh.tour.dto.response.UsuarioResponse;
import dh.tour.model.Tour;
import dh.tour.model.Usuario;
import dh.tour.repository.InscripcionRepository;
import dh.tour.repository.UsuarioRepository;
import dh.tour.service.InscripcionService;
import dh.tour.service.UsuarioService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor // <-- Agregamos esto para limpiar constructores
public class UsuarioController {

    private final UsuarioService usuarioService; // Única dependencia necesaria
    private final InscripcionService inscripcionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> getAll() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> actualizarUsuario(
            @PathVariable String id,
            @RequestBody Usuario usuarioRequest,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getId().equals(id)) {
            return ResponseEntity.status(403).body("No autorizado");
        }

        // ✅ USAMOS EL SERVICE: Esto quita el aviso de "Method not used"
        usuarioService.actualizar(id, usuarioRequest);
        return ResponseEntity.ok("Usuario actualizado correctamente");
    }

    // 🔹 Actualización parcial (PATCH) para el usuario
    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> actualizarParcial(
            @PathVariable String id,
            @RequestBody Map<String, Object> campos,
            @AuthenticationPrincipal CustomUserDetails principal) {

        // Seguridad: El usuario solo puede editar su propio perfil
        // Los ADMIN podrían editar a cualquiera si quitas esta validación
        if (!principal.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para editar este perfil");
        }
        usuarioService.actualizarParcial(id, campos);
        return ResponseEntity.ok("Se ha actualizado parcialmente correctamente el usuario");

    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
            usuarioService.eliminarUsuario(id);
           return ResponseEntity.ok("Se ha eliminado correctamente el usuario con id: " + id);
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

        if (!principal.getId().equals(id)) {
            return ResponseEntity.status(403)
                    .body("No puedes ver inscripciones de otros");
        }


        return ResponseEntity.ok(
                inscripcionService.obtenerInscripcionesUsuario(id)
        );
    }



}


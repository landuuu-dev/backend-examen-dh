package dh.tour.controllers;

import dh.tour.config.security.CustomUserDetails;
import dh.tour.dto.response.UsuarioResponse;
import dh.tour.model.Tour;
import dh.tour.model.Usuario;
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

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final InscripcionService inscripcionService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> getAll() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> actualizarUsuario(
            @PathVariable String id,
            @RequestBody Usuario usuarioRequest,
            @AuthenticationPrincipal CustomUserDetails principal) {

        boolean esMismoUsuario = principal.getId().equals(id);
        boolean esAdminOSuperAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                        a.getAuthority().equals("SUPER_ADMIN") ||
                        a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ADMIN"));

        if (!esMismoUsuario && !esAdminOSuperAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para modificar a este usuario");
        }

        usuarioService.actualizar(id, usuarioRequest);
        return ResponseEntity.ok("Usuario actualizado correctamente");
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> actualizarParcial(
            @PathVariable String id,
            @RequestBody Map<String, Object> campos,
            @AuthenticationPrincipal CustomUserDetails principal) {

        if (!principal.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para editar este perfil");
        }
        usuarioService.actualizarParcial(id, campos);
        return ResponseEntity.ok("Se ha actualizado parcialmente correctamente el usuario");
    }

    // 🎯 AQUÍ ESTABA EL BLOQUEO 403: Cambiado a hasAnyAuthority
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> delete(@PathVariable String id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok("Se ha eliminado correctamente el usuario con id: " + id);
    }

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
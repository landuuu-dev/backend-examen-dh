package dh.tour.controllers;

import dh.tour.config.security.CustomUserDetails;
import dh.tour.dto.request.TourRequest;
import dh.tour.dto.response.TourResponse;
import dh.tour.model.Inscripcion;
import dh.tour.service.InscripcionService;
import dh.tour.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;
    private final InscripcionService inscripcionService;

    @GetMapping
    public ResponseEntity<List<TourResponse>> getTours() {
        return ResponseEntity.ok(tourService.listarTodos());
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<?> createTour(
            @ModelAttribute TourRequest tourRequest, // Spring llena el DTO por ti
            @RequestParam(required = false) List<MultipartFile> imagenes) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(tourService.crearTour(tourRequest, imagenes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTourPut(
            @PathVariable String id,
            @ModelAttribute TourRequest tourRequest, // Usamos el DTO aquí también
            @RequestParam(required = false) List<MultipartFile> imagenes) {
        try {
            return ResponseEntity.ok(tourService.actualizarTour(id, tourRequest, imagenes));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTourPatch(
            @PathVariable String id,
            @ModelAttribute TourRequest tourRequest, // Agrupamos todo en el DTO
            @RequestParam(required = false) List<MultipartFile> imagenes) {
        try {
            // Usamos el mismo método del service que ya sabe manejar nulos
            return ResponseEntity.ok(tourService.actualizarTour(id, tourRequest, imagenes));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTour(@PathVariable String id) {
        try {
            tourService.eliminarTour(id);
            return ResponseEntity.ok("Tour eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/inscribir")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> inscribir(@PathVariable String id, @AuthenticationPrincipal CustomUserDetails principal) {
        try {
            String msg = inscripcionService.inscribirUsuario(id, principal.getId(), principal.getUsername());
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{tourId}/desinscribirse")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> desinscribirse(@PathVariable String tourId, @AuthenticationPrincipal CustomUserDetails principal) {
        try {
            inscripcionService.desinscribir(tourId, principal.getId());
            return ResponseEntity.ok("Desinscripción exitosa");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🔹 Endpoint para que el Admin vea quiénes se inscribieron a un tour específico
    @GetMapping("/{id}/inscritos")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<Inscripcion>> verInscritosEnTour(@PathVariable String id) {

        return ResponseEntity.ok(inscripcionService.obtenerInscripcionesPorTour(id));
    }


}

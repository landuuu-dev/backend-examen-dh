package dh.tour.controllers;

import dh.tour.config.security.CustomUserDetails;
import dh.tour.dto.request.TourRequest;
import dh.tour.dto.response.TourResponse;
import dh.tour.model.Inscripcion;
import dh.tour.service.InscripcionService;
import dh.tour.service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
            @Valid @ModelAttribute TourRequest tourRequest, // Spring llena el DTO por ti
            @RequestParam(required = false) List<MultipartFile> imagenes) throws IOException {
        tourService.crearTour(tourRequest, imagenes);
            return ResponseEntity.ok("Este tour fue creado con exito");
    }


    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTourPut(
            @PathVariable String id,
            @ModelAttribute TourRequest tourRequest, // Usamos el DTO aquí también
            @RequestParam(required = false) List<MultipartFile> imagenes) throws IOException {

        tourService.actualizarTour(id, tourRequest, imagenes);
        return ResponseEntity.ok("Tour actualizado correctamente");

    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTourPatch(
            @PathVariable String id,
            @ModelAttribute TourRequest tourRequest, // Agrupamos todo en el DTO
            @RequestParam(required = false) List<MultipartFile> imagenes) throws IOException {
        tourService.actualizarTour(id, tourRequest, imagenes);
            return ResponseEntity.ok("Tour actualizado parcialmente correctamente");
        }


    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTour(@PathVariable String id) {
        tourService.eliminarTour(id); // Si falla, el Handler lo atrapa
        return ResponseEntity.ok("Tour eliminado correctamente");
    }

    @PostMapping("/{id}/inscribir")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> inscribir(@PathVariable String id, @AuthenticationPrincipal CustomUserDetails principal) {
        inscripcionService.inscribirUsuario(id, principal.getId(), principal.getUsername());
        return ResponseEntity.ok("Te haz inscrito al Tour correctamente");
        }


    @DeleteMapping("/{tourId}/desinscribirse")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> desinscribirse(@PathVariable String tourId, @AuthenticationPrincipal CustomUserDetails principal) {
        inscripcionService.desinscribir(tourId, principal.getId());
        return ResponseEntity.ok("Desinscripción exitosa");
    }

    // 🔹 Endpoint para que el Admin vea quiénes se inscribieron a un tour específico
    @GetMapping("/{id}/inscritos")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<Inscripcion>> verInscritosEnTour(@PathVariable String id) {

        return ResponseEntity.ok(inscripcionService.obtenerInscripcionesPorTour(id));
    }
    @GetMapping("/search")
    public ResponseEntity<Page<TourResponse>> buscar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer precioMax,
            @PageableDefault(size = 10, sort = "precio") Pageable pageable
    ) {
        return ResponseEntity.ok(tourService.buscarTours(nombre, precioMax, pageable));
    }

}

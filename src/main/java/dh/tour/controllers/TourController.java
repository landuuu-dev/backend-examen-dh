package dh.tour.controllers;

import dh.tour.config.security.CustomUserDetails;
import dh.tour.model.Categoria;
import dh.tour.model.Inscripcion;
import dh.tour.model.Tour;
import dh.tour.repository.CategoriaRepository;
import dh.tour.repository.InscripcionRepository;
import dh.tour.repository.TourRepository;
import dh.tour.service.CloudinaryService;
import dh.tour.service.InscripcionService;
import dh.tour.service.TourService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(
        origins = "http://localhost:5173",
        allowedHeaders = "*",
        methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS }
)

@RestController
@RequestMapping("/tours")
public class TourController {

    private final TourService tourService;
    private final InscripcionRepository inscripcionRepository; // Se puede mover a un InscripcionService después
    private final TourRepository tourRepository;
    private final InscripcionService inscripcionService;

    public TourController(TourService tourService, InscripcionRepository inscripcionRepository, TourRepository tourRepository, InscripcionService inscripcionService) {
        this.tourService = tourService;
        this.inscripcionRepository = inscripcionRepository;
        this.tourRepository = tourRepository;
        this.inscripcionService = inscripcionService;
    }

    @GetMapping
    public ResponseEntity<List<Tour>> getTours() {
        return ResponseEntity.ok(tourService.listarTodos());
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<?> createTour(@RequestParam String nombre, @RequestParam String categoriaId,
                                        @RequestParam String descripcion, @RequestParam String ubicacion, @RequestParam int precio,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                        @RequestParam int cuposTotales, @RequestParam boolean disponible,
                                        @RequestParam(required = false) List<MultipartFile> imagenes) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(tourService.crearTour(nombre, categoriaId, descripcion, ubicacion, precio, fechaInicio, fechaFin, cuposTotales, disponible, imagenes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTourPut(@PathVariable String id,
                                           @RequestParam(required = false) String nombre,
                                           @RequestParam(required = false) String categoriaId,
                                           @RequestParam(required = false) String descripcion,
                                           @RequestParam(required = false) String ubicacion,
                                           @RequestParam(required = false) Integer precio,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                           @RequestParam(required = false) Integer cuposTotales,
                                           @RequestParam(required = false) Integer cuposDisponibles,
                                           @RequestParam(required = false) Boolean disponible,
                                           @RequestParam(required = false) List<MultipartFile> imagenes) {
        try {
            return ResponseEntity.ok(tourService.actualizarTour(id, nombre, descripcion, ubicacion, precio, fechaInicio, fechaFin, cuposTotales, cuposDisponibles, disponible, categoriaId, imagenes));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTour(@PathVariable String id, @RequestParam(required = false) String nombre,
                                        @RequestParam(required = false) String categoriaId, @RequestParam(required = false) String descripcion,
                                        @RequestParam(required = false) String ubicacion, @RequestParam(required = false) Integer precio,
                                        @RequestParam(required = false) LocalDate fechaInicio, @RequestParam(required = false) LocalDate fechaFin,
                                        @RequestParam(required = false) Integer cuposTotales, @RequestParam(required = false) Integer cuposDisponibles,
                                        @RequestParam(required = false) Boolean disponible, @RequestParam(required = false) List<MultipartFile> imagenes) {
        try {
            return ResponseEntity.ok(tourService.actualizarTour(id, nombre, descripcion, ubicacion, precio, fechaInicio, fechaFin, cuposTotales, cuposDisponibles, disponible, categoriaId, imagenes));
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
        // Buscamos directamente en el repository las inscripciones que coincidan con el ID del tour
        return ResponseEntity.ok(inscripcionRepository.findByTourId(id));
    }


}

package dh.tour.controllers;

import dh.tour.config.security.CustomUserDetails;
import dh.tour.model.Categoria;
import dh.tour.model.Inscripcion;
import dh.tour.model.Tour;
import dh.tour.repository.CategoriaRepository;
import dh.tour.repository.InscripcionRepository;
import dh.tour.repository.TourRepository;
import dh.tour.service.CloudinaryService;
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

    private final TourRepository tourRepository;
    private final CategoriaRepository categoriaRepository;
    private final CloudinaryService cloudinaryService;
    private final InscripcionRepository inscripcionRepository;


    public TourController(TourRepository tourRepository,
                          CategoriaRepository categoriaRepository,
                          CloudinaryService cloudinaryService, InscripcionRepository inscripcionRepository) {
        this.tourRepository = tourRepository;
        this.categoriaRepository = categoriaRepository;
        this.cloudinaryService = cloudinaryService;
        this.inscripcionRepository = inscripcionRepository;
    }

    // Listar todos los tours
    @GetMapping
    public ResponseEntity<List<Tour>> getTours() {
        return ResponseEntity.ok(tourRepository.findAll());
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Tour>> getByCategoria(@PathVariable String categoriaId) {
        return ResponseEntity.ok(tourRepository.findByCategoriaId(categoriaId));
    }

    // Crear tour
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<?> createTour(
            @RequestParam String nombre,
            @RequestParam String categoriaId,
            @RequestParam String descripcion,
            @RequestParam String ubicacion,
            @RequestParam int precio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam int cuposTotales,
            @RequestParam boolean disponible,
            @RequestParam(required = false) List<MultipartFile> imagenes) {

        // Buscar categoría
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElse(null);
        if (categoria == null)
            return ResponseEntity.badRequest().body("Categoría no encontrada");

        // Subir imágenes si hay
        List<String> urls = new ArrayList<>();
        if (imagenes != null) {
            for (MultipartFile img : imagenes) {
                if (!img.isEmpty()) {
                    try {
                        String url = cloudinaryService.uploadFile(img);
                        urls.add(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error subiendo las imágenes: " + e.getMessage());
                    }
                }
            }
        }

        // Dentro de createTour
        Tour tour = new Tour(nombre, categoria, descripcion, ubicacion, precio, fechaInicio, fechaFin, cuposTotales, disponible, urls);
        tour.setCuposDisponibles(cuposTotales); // Inicializa los disponibles igual a los totales
        return ResponseEntity.status(HttpStatus.CREATED).body(tourRepository.save(tour));
    }


    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTour(
            @PathVariable String id,
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

        return tourRepository.findById(id).map(tour -> {
            try {
                // Actualizamos solo lo que viene, igual que en el PATCH
                if (nombre != null) tour.setNombre(nombre);
                if (descripcion != null) tour.setDescripcion(descripcion);
                if (ubicacion != null) tour.setUbicacion(ubicacion);
                if (precio != null) tour.setPrecio(precio);
                if (fechaInicio != null) tour.setFechaInicio(fechaInicio);
                if (fechaFin != null) tour.setFechaFin(fechaFin);
                if (cuposTotales != null) tour.setCuposTotales(cuposTotales);
                if (cuposDisponibles != null) tour.setCuposDisponibles(cuposDisponibles);
                if (disponible != null) tour.setDisponible(disponible);

                if (categoriaId != null) {
                    Categoria cat = categoriaRepository.findById(categoriaId).orElse(null);
                    if (cat != null) tour.setCategoria(cat);
                }

                if (imagenes != null && !imagenes.isEmpty()) {
                    List<String> urls = new ArrayList<>();
                    for (MultipartFile img : imagenes) {
                        if (!img.isEmpty()) {
                            urls.add(cloudinaryService.uploadFile(img));
                        }
                    }
                    if (!urls.isEmpty()) tour.setImagenes(urls);
                }

                return ResponseEntity.ok(tourRepository.save(tour));

            } catch (IOException e) {
                return ResponseEntity.internalServerError().body("Error con las imágenes");
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // Borrar tour
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTour(@PathVariable String id) {
        return tourRepository.findById(id)
                .map(tour -> {
                    String nombreTour = tour.getNombre();
                    tourRepository.deleteById(id);
                    return ResponseEntity.ok("El tour: " + nombreTour + ", ha sido eliminado correctamente");
                })
                        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Tour con id: " + id + " No encontrado")
        );
    }

    //patch actualizar
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> actualizarParcial(
            @PathVariable String id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoriaId,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) Integer precio,
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) Integer cuposTotales,
            @RequestParam(required = false) Integer cuposDisponibles,
            @RequestParam(required = false) Boolean disponible,
            @RequestParam(required = false) List<MultipartFile> imagenes
    ) {


        return tourRepository.findById(id).map(tour -> {

            try {
                if (nombre != null && !nombre.isEmpty()) {
                    tour.setNombre(nombre);
                }

                if (descripcion != null && !descripcion.isEmpty()) {
                    tour.setDescripcion(descripcion);
                }

                if (ubicacion != null && !ubicacion.isEmpty()) {
                    tour.setUbicacion(ubicacion);
                }

                if (precio != null) {
                    tour.setPrecio(precio);
                }
                if(fechaInicio != null){
                    tour.setFechaInicio(fechaInicio);
                }
                if (fechaFin != null){
                    tour.setFechaFin(fechaFin);
                }
                if(cuposTotales != null){
                    tour.setCuposTotales(cuposTotales);
                }
                if(cuposDisponibles != null){
                    tour.setCuposDisponibles(cuposDisponibles);
                }
                if (disponible != null){
                    tour.setDisponible(disponible);
                }

                if (categoriaId != null && !categoriaId.isEmpty()) {
                    Categoria categoria = categoriaRepository.findById(categoriaId).orElse(null);
                    if (categoria == null) {
                        return ResponseEntity.badRequest().body("Categoría no encontrada");
                    }
                    tour.setCategoria(categoria);
                }

                // Reemplazar imágenes SOLO si se envían
                if (imagenes != null && !imagenes.isEmpty()) {
                    List<String> urls = new ArrayList<>();
                    for (MultipartFile img : imagenes) {
                        if (!img.isEmpty()) {
                            String url = cloudinaryService.uploadFile(img);
                            urls.add(url);
                        }
                    }
                    if (!urls.isEmpty()) {
                        tour.setImagenes(urls);
                    }
                }

                tourRepository.save(tour);
                return ResponseEntity.ok(tour);

            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body("Error al actualizar el tour");
            }

        }).orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para inscribirse a tours (Solo usuarios logueados)
    @PostMapping("/{id}/inscribir")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> inscribirUsuario(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails principal) {

        return tourRepository.findById(id).map(tour -> {
            // Dentro de inscribirUsuario en TourController
            boolean yaInscrito = inscripcionRepository.existsByUsuarioIdAndTourId(principal.getId(), tour.getId());
            if (yaInscrito) {
                return ResponseEntity.badRequest().body("Ya estás inscrito en " + tour.getNombre() + " no puedes inscribirte de nuevo");
            }

            // 1. Validar disponibilidad y fechas
            if (!tour.checkDisponibilidad()) {
                return ResponseEntity.badRequest().body("El tour: " + tour.getNombre() + " ya no está disponible o venció la fecha");
            }

            // 2. Validar cupos
            if (tour.getCuposDisponibles() <= 0) {
                return ResponseEntity.badRequest().body("No hay cupos disponibles en "+ tour.getNombre());
            }

            // 3. Registrar Inscripción (Necesitarás un InscripcionRepository)
            Inscripcion inscripcion = new Inscripcion();
            inscripcion.setTourId(tour.getId());
            inscripcion.setUsuarioId(principal.getId());
            inscripcion.setNombreUsuario(principal.getUsername());
            inscripcion.setNombreTour(tour.getNombre());
            inscripcion.setFechaInscripcion(LocalDateTime.now());

            inscripcionRepository.save(inscripcion);

            // 4. Descontar cupo
            tour.setCuposDisponibles(tour.getCuposDisponibles() - 1);
            tour.checkDisponibilidad(); // Actualiza el boolean si llegó a 0
            tourRepository.save(tour);

            return ResponseEntity.ok("Inscripción exitosa al tour: " + tour.getNombre());
        }).orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para que el Admin vea inscritos
    @GetMapping("/{id}/inscritos")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<Inscripcion>> verInscritosEnTour(@PathVariable String id) {
        return ResponseEntity.ok(inscripcionRepository.findByTourId(id));
    }

    @DeleteMapping("/{tourId}/desinscribirse")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> desinscribirse(
            @PathVariable String tourId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        String usuarioId = principal.getId();

        Inscripcion inscripcion = inscripcionRepository
                .findByTourIdAndUsuarioId(tourId, usuarioId)
                .orElseThrow(() ->
                        new RuntimeException("No estás inscrito en este tour"));

        // Eliminar inscripción
        inscripcionRepository.delete(inscripcion);

        // Buscar nombre del tour (ya lo tienes en la inscripción)
        String nombreTour = inscripcion.getNombreTour();

        return ResponseEntity.ok(
                "Te desinscribiste correctamente del tour: " + nombreTour
        );
    }


}

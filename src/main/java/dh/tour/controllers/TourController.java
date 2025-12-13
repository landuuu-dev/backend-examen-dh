package dh.tour.controllers;

import dh.tour.model.Categoria;
import dh.tour.model.Tour;
import dh.tour.repository.CategoriaRepository;
import dh.tour.repository.TourRepository;
import dh.tour.service.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    public TourController(TourRepository tourRepository,
                          CategoriaRepository categoriaRepository,
                          CloudinaryService cloudinaryService) {
        this.tourRepository = tourRepository;
        this.categoriaRepository = categoriaRepository;
        this.cloudinaryService = cloudinaryService;
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
    @PostMapping
    public ResponseEntity<?> createTour(
            @RequestParam String nombre,
            @RequestParam String categoriaId,
            @RequestParam String descripcion,
            @RequestParam String ubicacion,
            @RequestParam int precio,
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

        Tour tour = new Tour(nombre, categoria, descripcion, ubicacion, precio, urls);
        return ResponseEntity.status(HttpStatus.CREATED).body(tourRepository.save(tour));
    }


    // Actualizar tour
    // Actualizar tour con form-data y subida de imágenes
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTour(
            @PathVariable String id,
            @RequestParam String nombre,
            @RequestParam String categoriaId,
            @RequestParam String descripcion,
            @RequestParam String ubicacion,
            @RequestParam int precio,
            @RequestParam(required = false) List<MultipartFile> imagenes) {

        // Buscar tour
        return tourRepository.findById(id).map(tour -> {

            // Buscar categoría
            Categoria categoria = categoriaRepository.findById(categoriaId).orElse(null);
            if (categoria == null) return ResponseEntity.badRequest().body("Categoría no encontrada");

            tour.setNombre(nombre);
            tour.setCategoria(categoria);
            tour.setDescripcion(descripcion);
            tour.setUbicacion(ubicacion);
            tour.setPrecio(precio);

            // Subir nuevas imágenes si se enviaron
            if (imagenes != null && !imagenes.isEmpty()) {
                List<String> urls = new ArrayList<>();
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
                tour.setImagenes(urls); // reemplaza las imágenes antiguas
            }

            return ResponseEntity.ok(tourRepository.save(tour));

        }).orElseGet(() ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tour con id " + id + " no encontrado")
        );
    }


    // Borrar tour
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTour(@PathVariable String id) {
        if (!tourRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Tour con id " + id + " no encontrado");
        }
        tourRepository.deleteById(id);
        return ResponseEntity.ok("Tour eliminado correctamente");
    }

    //patch actualizar
    @PatchMapping("/{id}")
    public ResponseEntity<?> actualizarParcial(
            @PathVariable String id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoriaId,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) Integer precio,
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

}

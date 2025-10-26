package dh.tour.controllers;

import dh.tour.model.Tour;
import dh.tour.repository.TourRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tours")
public class TourController {

    private final TourRepository tourRepository;

    public TourController(TourRepository tourRepository) {
        this.tourRepository = tourRepository;
    }

    // Listar todos los tours
    @GetMapping
    public ResponseEntity<List<Tour>> getTours() {
        List<Tour> tours = tourRepository.findAll();
        return ResponseEntity.ok(tours);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Tour>> getByCategoria(@PathVariable String categoriaId) {
        List<Tour> tours = tourRepository.findByCategoriaId(categoriaId);
        return ResponseEntity.ok(tours);
    }

    // Crear un tour con validación de nombre único
    @PostMapping
    public ResponseEntity<?> createTour(@RequestBody Tour tour) {
        // Validación: nombre obligatorio
        if (tour.getNombre() == null || tour.getNombre().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El nombre del tour es obligatorio.");
        }

        // Validación: categoría obligatoria
        if (tour.getCategoria() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El tour debe tener una categoría.");
        }

        // Validación: nombre único (ignora mayúsculas/minúsculas)
        boolean existe = tourRepository.existsByNombreIgnoreCase(tour.getNombre().trim());
        if (existe) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Ya existe un tour con ese nombre.");
        }

        Tour creado = tourRepository.save(tour);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // Actualizar un tour con validación de nombre único
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTour(@PathVariable String id, @RequestBody Tour tourActualizado) {
        return tourRepository.findById(id)
                .map(tour -> {
                    // Validación: categoría obligatoria
                    if (tourActualizado.getCategoria() == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("El tour debe tener una categoría.");
                    }

                    // Validación: nombre obligatorio
                    if (tourActualizado.getNombre() == null || tourActualizado.getNombre().trim().isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("El nombre del tour es obligatorio.");
                    }

                    // Validación: nombre único (excepto este mismo tour)
                    boolean existeOtro = tourRepository.existsByNombreIgnoreCaseAndIdNot(
                            tourActualizado.getNombre().trim(), id);
                    if (existeOtro) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Ya existe otro tour con ese nombre.");
                    }

                    // Actualizar
                    tour.setNombre(tourActualizado.getNombre());
                    tour.setCategoria(tourActualizado.getCategoria());
                    tour.setDescripcion(tourActualizado.getDescripcion());
                    tour.setUbicacion(tourActualizado.getUbicacion());
                    tour.setPrecio(tourActualizado.getPrecio());
                    tour.setImagenes(tourActualizado.getImagenes());

                    Tour actualizado = tourRepository.save(tour);
                    return ResponseEntity.ok(actualizado);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Tour con id " + id + " no encontrado."));
    }

    // Eliminar un tour
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTour(@PathVariable String id) {
        if (tourRepository.existsById(id)) {
            tourRepository.deleteById(id);
            return ResponseEntity.ok("Tour con id " + id + " eliminado correctamente");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Tour con id " + id + " no encontrado, no se pudo eliminar");
        }
    }
}

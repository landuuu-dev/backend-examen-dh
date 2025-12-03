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
        return ResponseEntity.ok(tourRepository.findAll());
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Tour>> getByCategoria(@PathVariable String categoriaId) {
        return ResponseEntity.ok(tourRepository.findByCategoriaId(categoriaId));
    }

    // Crear tour
    @PostMapping
    public ResponseEntity<?> createTour(@RequestBody Tour tour) {

        if (tour.getNombre() == null || tour.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre del tour es obligatorio.");
        }

        if (tour.getCategoria() == null) {
            return ResponseEntity.badRequest().body("El tour debe tener una categoría.");
        }

        if (tourRepository.existsByNombreIgnoreCase(tour.getNombre().trim())) {
            return ResponseEntity.badRequest().body("Ya existe un tour con ese nombre.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(tourRepository.save(tour));
    }

    // Actualizar tour
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTour(@PathVariable String id, @RequestBody Tour tourActualizado) {

        return tourRepository.findById(id).map(tour -> {

            if (tourActualizado.getCategoria() == null) {
                return ResponseEntity.badRequest().body("El tour debe tener una categoría.");
            }

            if (tourActualizado.getNombre() == null || tourActualizado.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre del tour es obligatorio.");
            }

            boolean existe = tourRepository.existsByNombreIgnoreCaseAndIdNot(
                    tourActualizado.getNombre().trim(), id);

            if (existe) {
                return ResponseEntity.badRequest().body("Ya existe otro tour con ese nombre.");
            }

            tour.setNombre(tourActualizado.getNombre());
            tour.setCategoria(tourActualizado.getCategoria());
            tour.setDescripcion(tourActualizado.getDescripcion());
            tour.setUbicacion(tourActualizado.getUbicacion());
            tour.setPrecio(tourActualizado.getPrecio());
            tour.setImagenes(tourActualizado.getImagenes());

            return ResponseEntity.ok(tourRepository.save(tour));

        }).orElseGet(() ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Tour con id " + id + " no encontrado.")
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
}
